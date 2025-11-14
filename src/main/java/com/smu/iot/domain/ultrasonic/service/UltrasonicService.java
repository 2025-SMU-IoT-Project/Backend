package com.smu.iot.domain.ultrasonic.service;

import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.ultrasonic.code.UltrasonicErrorCode;
import com.smu.iot.domain.ultrasonic.dto.request.UltrasonicRequestDTO;
import com.smu.iot.domain.ultrasonic.dto.response.BinFillRateResponseDTO;
import com.smu.iot.domain.ultrasonic.dto.response.UltrasonicResponseDTO;
import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import com.smu.iot.domain.ultrasonic.repository.UltrasonicRepository;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UltrasonicService {

    private final UltrasonicRepository ultrasonicRepository;
    private final BinRepository binRepository;

    private static final double BIN_HEIGHT = 50.0; // cm
    private static final double COLLECTION_THRESHOLD = 80.0; // 수거 필요 임계값 (%)

    @Transactional
    public UltrasonicResponseDTO processUltrasonicData(UltrasonicRequestDTO request) {
        // 입력 검증
        validateRequest(request);

        // 채움률 계산 (요청에 포함되지 않은 경우)
        double fillRate = request.getFillRate() != null
            ? request.getFillRate()
            : calculateFillRate(request.getDistanceCm());

        // 엔티티 생성 및 저장
        Ultrasonic ultrasonic = Ultrasonic.builder()
            .binId(request.getBinId())
            .uuid(request.getUuid())
            .distanceCm(request.getDistanceCm())
            .fillRate(fillRate)
            .build();

        Ultrasonic saved = ultrasonicRepository.save(ultrasonic);

        return convertToResponseDTO(saved);
    }

    public BinFillRateResponseDTO getCurrentFillRate(Long binId) {
        // 최신 데이터 조회
        Ultrasonic latest = ultrasonicRepository.findFirstByBinIdOrderByCreatedAtDesc(binId)
            .orElseThrow(() -> new GeneralException(UltrasonicErrorCode.NO_DATA_FOUND));

        // 수거 필요 여부 판단
        boolean needsCollection = latest.getFillRate() >= COLLECTION_THRESHOLD;

        return BinFillRateResponseDTO.builder()
            .binId(latest.getBinId())
            .uuid(latest.getUuid())
            .distanceCm(latest.getDistanceCm())
            .fillRate(latest.getFillRate())
            .binHeight(BIN_HEIGHT)
            .needsCollection(needsCollection)
            .collectionThreshold(COLLECTION_THRESHOLD)
            .lastUpdated(latest.getCreatedAt())
            .build();
    }

    public List<UltrasonicResponseDTO> getFillRateHistory(Long binId, int limit) {
        // limit 제한 (1-20)
        int validLimit = Math.max(1, Math.min(limit, 20));

        // 최근 데이터 조회
        Pageable pageable = PageRequest.of(0, validLimit);
        List<Ultrasonic> history = ultrasonicRepository.findByBinIdOrderByCreatedAtDesc(binId);

        // 최대 limit 개수만큼만 반환
        return history.stream()
            .limit(validLimit)
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }

    public UltrasonicResponseDTO getDataByUuid(String uuid) {
        Ultrasonic data = ultrasonicRepository.findByUuid(uuid)
            .orElseThrow(() -> new GeneralException(UltrasonicErrorCode.UUID_NOT_FOUND));

        return convertToResponseDTO(data);
    }

    // 채움률 계산: (쓰레기통 높이 - 측정 거리) / 쓰레기통 높이 * 100
    private double calculateFillRate(double distanceCm) {
        if (distanceCm >= BIN_HEIGHT) {
            return 0.0;
        }
        if (distanceCm < 0) {
            throw new GeneralException(UltrasonicErrorCode.INVALID_DISTANCE);
        }

        double fillRate = ((BIN_HEIGHT - distanceCm) / BIN_HEIGHT) * 100;
        return Math.round(fillRate * 10.0) / 10.0; // 소수점 1자리로 반올림
    }

    private void validateRequest(UltrasonicRequestDTO request) {
        if (request.getBinId() == null) {
            throw new GeneralException(UltrasonicErrorCode.INVALID_REQUEST);
        }
        if (request.getDistanceCm() == null || request.getDistanceCm() < 0) {
            throw new GeneralException(UltrasonicErrorCode.INVALID_DISTANCE);
        }
        if (request.getUuid() == null || request.getUuid().isEmpty()) {
            throw new GeneralException(UltrasonicErrorCode.INVALID_REQUEST);
        }
    }

    private UltrasonicResponseDTO convertToResponseDTO(Ultrasonic ultrasonic) {
        return UltrasonicResponseDTO.builder()
            .binId(ultrasonic.getBinId())
            .uuid(ultrasonic.getUuid())
            .distanceCm(ultrasonic.getDistanceCm())
            .fillRate(ultrasonic.getFillRate())
            .createdAt(ultrasonic.getCreatedAt())
            .build();
    }
}
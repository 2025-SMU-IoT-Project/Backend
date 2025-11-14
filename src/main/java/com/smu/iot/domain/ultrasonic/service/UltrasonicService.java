package com.smu.iot.domain.ultrasonic.service;

import com.smu.iot.domain.ultrasonic.dto.request.UltrasonicRequestDTO;
import com.smu.iot.domain.ultrasonic.dto.response.BinFillRateResponseDTO;
import com.smu.iot.domain.ultrasonic.dto.response.UltrasonicResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UltrasonicService {

    private static final double BIN_HEIGHT = 50.0; // cm

    // Mock: 초음파 센서 데이터 처리
    public UltrasonicResponseDTO processUltrasonicData(UltrasonicRequestDTO request) {
        // 실제 구현 시: DB에 저장하고 저장된 데이터 반환
        // 현재는 Mock으로 요청 받은 데이터를 그대로 반환

        return UltrasonicResponseDTO.builder()
            .binId(request.getBinId())
            .uuid(request.getUuid())
            .distanceCm(request.getDistanceCm())
            .fillRate(request.getFillRate())
            .build();
    }

    // Mock: 현재 채움률 조회
    public BinFillRateResponseDTO getCurrentFillRate(Long binId) {
        // Mock 데이터 생성
        double distanceCm = 20.5;
        double fillRate = calculateFillRate(distanceCm);

        return BinFillRateResponseDTO.builder()
            .binId(binId)
            .uuid("latest-uuid-" + binId)
            .distanceCm(distanceCm)
            .fillRate(fillRate)
            .build();
    }

    // Mock: 채움률 이력 조회
    public List<UltrasonicResponseDTO> getFillRateHistory(Long binId, int limit) {
        List<UltrasonicResponseDTO> history = new ArrayList<>();

        // Mock 데이터 생성 (시간 역순)
        for (int i = 0; i < Math.min(limit, 20); i++) {
            double distanceCm = 45.0 - (i * 1.5); // 점점 차오르는 형태
            double fillRate = calculateFillRate(distanceCm);

            history.add(UltrasonicResponseDTO.builder()
                .binId(binId)
                .uuid("uuid-" + System.currentTimeMillis() + "-" + i)
                .distanceCm(distanceCm)
                .fillRate(fillRate)
                .build());
        }

        return history;
    }

    // Mock: UUID로 데이터 조회
    public UltrasonicResponseDTO getDataByUuid(String uuid) {
        return UltrasonicResponseDTO.builder()
            .binId(1L)
            .uuid(uuid)
            .distanceCm(25.3)
            .fillRate(49.4)
            .build();
    }

    // 채움률 계산 (쓰레기통 높이 - 측정 거리) / 쓰레기통 높이 * 100
    private double calculateFillRate(double distanceCm) {
        if (distanceCm >= BIN_HEIGHT) {
            return 0.0;
        }
        double fillRate = ((BIN_HEIGHT - distanceCm) / BIN_HEIGHT) * 100;
        return Math.round(fillRate * 10.0) / 10.0; // 소수점 1자리
    }
}
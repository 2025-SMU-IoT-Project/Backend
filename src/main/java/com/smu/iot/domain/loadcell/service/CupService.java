package com.smu.iot.domain.loadcell.service;

import com.smu.iot.domain.bin.dto.response.SensorHistoryResponseDTO;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.event.service.EventService;
import com.smu.iot.domain.loadcell.code.CupErrorCode;
import com.smu.iot.domain.loadcell.dto.request.BinWeightInitRequestDTO;
import com.smu.iot.domain.loadcell.dto.request.CupRequestDTO;
import com.smu.iot.domain.loadcell.dto.response.BinWeightHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupResponseDTO;
import com.smu.iot.domain.loadcell.dto.response.CupStatsDTO;
import com.smu.iot.domain.loadcell.entity.BinWeight;
import com.smu.iot.domain.loadcell.entity.BinWeightHistory;
import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.loadcell.entity.Cup.CupWeightType;
import com.smu.iot.domain.loadcell.repository.BinWeightHistoryRepository;
import com.smu.iot.domain.loadcell.repository.BinWeightRepository;
import com.smu.iot.domain.loadcell.repository.CupRepository;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CupService {

    private final CupRepository cupRepository;
    private final BinRepository binRepository;
    private final BinWeightRepository binWeightRepository;
    private final BinWeightHistoryRepository binWeightHistoryRepository;
    private final EventService eventService;

    // 컵통 초기화 (빈 컵통 무게 설정)
    @Transactional
    public void initializeBinWeight(BinWeightInitRequestDTO request) {
        if (request.getBinId() == null) {
            throw new GeneralException(CupErrorCode.INVALID_REQUEST);
        }
        if (request.getTareWeight() == null || request.getTareWeight() < 0) {
            throw new GeneralException(CupErrorCode.INVALID_WEIGHT);
        }

        BinWeight binWeight = binWeightRepository.findByBinId(request.getBinId())
            .orElseGet(() -> BinWeight.builder()
                .binId(request.getBinId())
                .build());

        binWeight.setTareWeight(request.getTareWeight());
        binWeight.setCurrentWeight(request.getTareWeight());
        binWeight.setPreviousWeight(request.getTareWeight());
        binWeight.setIsInitialized(true);

        binWeightRepository.save(binWeight);
        log.info("BinWeight initialized - binId: {}, tareWeight: {}g",
            request.getBinId(), request.getTareWeight());
    }

    // 컵통 무게 데이터 처리
    @Transactional
    public CupResponseDTO processWeightData(CupRequestDTO request) {
        validateRequest(request);

        Long binId = request.getBinId();
        Double currentBinWeight = request.getWeight();

        BinWeight binWeight = binWeightRepository.findByBinId(binId)
            .orElseThrow(() -> {
                log.error("BinWeight not initialized for binId: {}", binId);
                return new GeneralException(CupErrorCode.BIN_NOT_INITIALIZED);
            });

        if (!binWeight.getIsInitialized()) {
            log.error("BinWeight not initialized for binId: {}", binId);
            throw new GeneralException(CupErrorCode.BIN_NOT_INITIALIZED);
        }

        Double previousWeight = binWeight.getCurrentWeight();
        Double cupWeight = currentBinWeight - previousWeight;

        if (cupWeight < 0) {
            log.warn("Invalid cup weight detected - binId: {}, cupWeight: {}g (current: {}g, previous: {}g)",
                binId, cupWeight, currentBinWeight, previousWeight);
            throw new GeneralException(CupErrorCode.INVALID_WEIGHT);
        }

        CupWeightType cupType = CupWeightType.fromWeight(cupWeight);

        // Cup 엔티티 저장
        Cup cup = Cup.builder()
            .uuid(request.getUuid())
            .binId(binId)
            .weight(cupWeight)
            .isLiquid(cupWeight > 20.0)
            .cupType(cupType)
            .baseWeight(null)
            .liquidWeight(cupWeight > 20.0 ? cupWeight - 5.0 : 0.0)
            .weightThreshold(null)
            .liquidThreshold(null)
            .build();

        Cup savedCup = cupRepository.save(cup);
        log.info("Cup data saved - id: {}, binId: {}, uuid: {}, cupWeight: {}g, cupType: {}",
            savedCup.getId(), savedCup.getBinId(), savedCup.getUuid(),
            savedCup.getWeight(), savedCup.getCupType());

        // BinWeightHistory 저장
        BinWeightHistory history = BinWeightHistory.builder()
            .binId(binId)
            .currentWeight(currentBinWeight)
            .addedWeight(cupWeight)
            .uuid(request.getUuid())
            .cupId(savedCup.getId())
            .build();

        binWeightHistoryRepository.save(history);
        log.info("BinWeightHistory saved - binId: {}, currentWeight: {}g, addedWeight: {}g",
            binId, currentBinWeight, cupWeight);

        // BinWeight 업데이트 (현재 상태만 유지)
        binWeight.setPreviousWeight(previousWeight);
        binWeight.setCurrentWeight(currentBinWeight);
        binWeightRepository.save(binWeight);
        log.info("BinWeight updated - binId: {}, previousWeight: {}g, currentWeight: {}g",
            binId, previousWeight, currentBinWeight);

        // LIVE UUID 처리: Event 생성 건너뛰고 Bin 상태 직접 업데이트
        if ("LIVE".equals(request.getUuid())) {
            Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new GeneralException(CupErrorCode.BIN_NOT_FOUND));
            bin.addWeight(cupWeight);
            binRepository.save(bin);
        } else {
            updateMainEvent(request.getUuid(), savedCup);
        }

        return convertToResponseDTO(savedCup);
    }

    // 컵통 무게 리셋
    @Transactional
    public void resetBinWeight(Long binId) {
        BinWeight binWeight = binWeightRepository.findByBinId(binId)
            .orElseThrow(() -> new GeneralException(CupErrorCode.BIN_NOT_FOUND));

        if (!binWeight.getIsInitialized()) {
            throw new GeneralException(CupErrorCode.BIN_NOT_INITIALIZED);
        }

        binWeight.setCurrentWeight(binWeight.getTareWeight());
        binWeight.setPreviousWeight(binWeight.getTareWeight());
        binWeightRepository.save(binWeight);

        log.info("BinWeight reset to tare weight - binId: {}, tareWeight: {}g",
            binId, binWeight.getTareWeight());
    }

    // 컵통 무게 히스토리 조회
    public List<BinWeightHistoryDTO> getBinWeightHistory(Long binId, int limit) {
        int validLimit = Math.max(1, Math.min(limit, 100));

        List<BinWeightHistory> history = binWeightHistoryRepository.findByBinIdOrderByCreatedAtDesc(binId);

        return history.stream()
            .limit(validLimit)
            .map(this::convertToBinWeightHistoryDTO)
            .collect(Collectors.toList());
    }

    // 시간 범위별 컵통 무게 히스토리 조회 (그래프용)
    public List<BinWeightHistoryDTO> getBinWeightHistoryByTimeRange(
        Long binId, LocalDateTime startTime, LocalDateTime endTime) {

        List<BinWeightHistory> history = binWeightHistoryRepository
            .findByBinIdAndTimeRange(binId, startTime, endTime);

        return history.stream()
            .map(this::convertToBinWeightHistoryDTO)
            .collect(Collectors.toList());
    }

    private void updateMainEvent(String uuid, Cup cupData) {
        eventService.registerSensorData(
            uuid,
            cupData.getBinId(),
            EventService.SensorDataType.CUP,
            cupData
        );
    }

    public List<CupHistoryDTO> getWeightHistory(Long binId, int limit) {
        int validLimit = Math.max(1, Math.min(limit, 20));
        List<Cup> history = cupRepository.findByBinIdOrderByCreatedAtDesc(binId);

        return history.stream()
            .limit(validLimit)
            .map(this::convertToHistoryDTO)
            .collect(Collectors.toList());
    }

    public CupStatsDTO getWeightStats(Long binId) {
        Long totalCups = cupRepository.countByBinId(binId);
        if (totalCups == 0) {
            return createEmptyStats();
        }

        Long emptyCupCount = cupRepository.countByBinIdAndCupType(binId, CupWeightType.EMPTY_CUP);
        Long lightLiquidCount = cupRepository.countByBinIdAndCupType(binId, CupWeightType.LIGHT_LIQUID);
        Long mediumLiquidCount = cupRepository.countByBinIdAndCupType(binId, CupWeightType.MEDIUM_LIQUID);
        Long heavyLiquidCount = cupRepository.countByBinIdAndCupType(binId, CupWeightType.HEAVY_LIQUID);
        Long abnormalCount = cupRepository.countByBinIdAndCupType(binId, CupWeightType.ABNORMAL);

        Long emptyCups = emptyCupCount;
        long liquidCups = lightLiquidCount + mediumLiquidCount + heavyLiquidCount;

        List<CupWeightType> liquidTypes = Arrays.asList(
            CupWeightType.LIGHT_LIQUID,
            CupWeightType.MEDIUM_LIQUID,
            CupWeightType.HEAVY_LIQUID,
            CupWeightType.ABNORMAL
        );
        Double avgLiquidWeight = cupRepository.getAverageWeightByCupTypes(binId, liquidTypes);
        Double heaviestCup = cupRepository.getMaxWeight(binId);
        Double totalLiquidWeight = cupRepository.getTotalLiquidWeightByCupTypes(binId, liquidTypes);

        double liquidRate = ((double) liquidCups / totalCups.doubleValue()) * 100;
        liquidRate = Math.round(liquidRate * 100.0) / 100.0;

        CupStatsDTO.WeightRangeStats weightRangeStats = CupStatsDTO.WeightRangeStats.builder()
            .emptyCupCount(emptyCupCount.intValue())
            .lightLiquidCount(lightLiquidCount.intValue())
            .mediumLiquidCount(mediumLiquidCount.intValue())
            .heavyLiquidCount(heavyLiquidCount.intValue())
            .abnormalCount(abnormalCount.intValue())
            .build();

        return CupStatsDTO.builder()
            .totalCups(totalCups.intValue())
            .emptyCups(emptyCups.intValue())
            .liquidCups((int) liquidCups)
            .liquidRate(liquidRate)
            .totalLiquidWeight(totalLiquidWeight)
            .avgLiquidWeight(avgLiquidWeight != null ? Math.round(avgLiquidWeight * 10.0) / 10.0 : 0.0)
            .heaviestCup(heaviestCup != null ? heaviestCup : 0.0)
            .weightRangeStats(weightRangeStats)
            .build();
    }

    public CupResponseDTO getWeightByUuid(String uuid) {
        Cup cup = cupRepository.findByUuid(uuid)
            .orElseThrow(() -> new GeneralException(CupErrorCode.UUID_NOT_FOUND));

        return convertToResponseDTO(cup);
    }

    private void validateRequest(CupRequestDTO request) {
        if (request.getUuid() == null || request.getUuid().isEmpty()) {
            throw new GeneralException(CupErrorCode.INVALID_REQUEST);
        }
        if (request.getBinId() == null) {
            throw new GeneralException(CupErrorCode.INVALID_REQUEST);
        }
        if (request.getWeight() == null || request.getWeight() < 0) {
            throw new GeneralException(CupErrorCode.INVALID_WEIGHT);
        }
    }

    private CupStatsDTO createEmptyStats() {
        return CupStatsDTO.builder()
            .totalCups(0)
            .emptyCups(0)
            .liquidCups(0)
            .liquidRate(0.0)
            .totalLiquidWeight(0.0)
            .avgLiquidWeight(0.0)
            .heaviestCup(0.0)
            .weightRangeStats(CupStatsDTO.WeightRangeStats.builder()
                .emptyCupCount(0)
                .lightLiquidCount(0)
                .mediumLiquidCount(0)
                .heavyLiquidCount(0)
                .abnormalCount(0)
                .build())
            .build();
    }

    private CupResponseDTO convertToResponseDTO(Cup cup) {
        return CupResponseDTO.builder()
            .recordId(cup.getId())
            .uuid(cup.getUuid())
            .binId(cup.getBinId())
            .weight(cup.getWeight())
            .timestamp(cup.getCreatedAt())
            .build();
    }

    private CupHistoryDTO convertToHistoryDTO(Cup cup) {
        return CupHistoryDTO.builder()
            .recordId(cup.getId())
            .uuid(cup.getUuid())
            .weight(cup.getWeight())
            .isLiquid(cup.getIsLiquid())
            .cupType(cup.getCupType().name())
            .timestamp(cup.getCreatedAt())
            .build();
    }

    private BinWeightHistoryDTO convertToBinWeightHistoryDTO(BinWeightHistory history) {
        return BinWeightHistoryDTO.builder()
            .id(history.getId())
            .binId(history.getBinId())
            .currentWeight(history.getCurrentWeight())
            .addedWeight(history.getAddedWeight())
            .uuid(history.getUuid())
            .timestamp(history.getCreatedAt())
            .build();
    }

    public Object getBinWeightHistory(Long binId, String uuid, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 1);
        List<BinWeightHistory> histories;

        if (uuid != null) {
            histories = binWeightHistoryRepository.findByBinIdAndUuidOrderByCreatedAtDesc(binId, uuid, pageable);
        } else {
            histories = binWeightHistoryRepository.findByBinIdOrderByCreatedAtDesc(binId, pageable);
        }

        if (limit == null) {
            return histories.isEmpty() ? null : convertToSensorHistoryDTO(histories.get(0));
        }

        return histories.stream()
            .map(this::convertToSensorHistoryDTO)
            .collect(Collectors.toList());
    }

    private SensorHistoryResponseDTO convertToSensorHistoryDTO(BinWeightHistory history) {
        return SensorHistoryResponseDTO.builder()
            .timestamp(history.getCreatedAt())
            .value(history.getCurrentWeight())
            .build();
    }
}
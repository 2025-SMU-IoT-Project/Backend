package com.smu.iot.domain.loadcell.service;

import com.smu.iot.domain.loadcell.code.CupErrorCode;
import com.smu.iot.domain.loadcell.dto.request.CupRequestDTO;
import com.smu.iot.domain.loadcell.dto.response.CupHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupResponseDTO;
import com.smu.iot.domain.loadcell.dto.response.CupStatsDTO;
import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.loadcell.entity.Cup.CupWeightType;
import com.smu.iot.domain.loadcell.repository.CupRepository;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CupService {

    private final CupRepository cupRepository;

    // 기본 설정값
    private static final double DEFAULT_BASE_WEIGHT = 5.5;        // 빈 컵 기준 무게 (g)
    private static final double DEFAULT_WEIGHT_THRESHOLD = 1.0;   // 무게 감지 임계값 (g)
    private static final double DEFAULT_LIQUID_THRESHOLD = 20.0;  // 액체 판별 임계값 (g)

    @Transactional
    public CupResponseDTO processWeightData(CupRequestDTO request) {
        // 입력 검증
        validateRequest(request);

        // 무게 타입 결정
        CupWeightType cupType = CupWeightType.fromWeight(request.getWeight());

        // 기본값 설정
        Double baseWeight = request.getBaseWeight() != null
            ? request.getBaseWeight()
            : DEFAULT_BASE_WEIGHT;

        Double weightThreshold = request.getWeightThreshold() != null
            ? request.getWeightThreshold()
            : DEFAULT_WEIGHT_THRESHOLD;

        Double liquidThreshold = request.getLiquidThreshold() != null
            ? request.getLiquidThreshold()
            : DEFAULT_LIQUID_THRESHOLD;

        // 액체 무게 계산
        double liquidWeight = 0.0;
        if (request.getIsliquid()) {
            liquidWeight = request.getWeight() - baseWeight;
            if (liquidWeight < 0) {
                liquidWeight = 0.0;
            }
        }

        // 엔티티 생성 및 저장
        Cup cup = Cup.builder()
            .uuid(request.getUuid())
            .binId(request.getBinId())
            .weight(request.getWeight())
            .isLiquid(request.getIsliquid())
            .cupType(cupType)
            .baseWeight(baseWeight)
            .liquidWeight(liquidWeight)
            .weightThreshold(weightThreshold)
            .liquidThreshold(liquidThreshold)
            .build();

        Cup saved = cupRepository.save(cup);
        log.info("LoadCell data saved - id: {}, binId: {}, uuid: {}, weight: {}g, isLiquid: {}, cupType: {}",
            saved.getId(), saved.getBinId(), saved.getUuid(), saved.getWeight(),
            saved.getIsLiquid(), saved.getCupType());

        return convertToResponseDTO(saved);
    }

    public List<CupHistoryDTO> getWeightHistory(Long binId, int limit) {
        int validLimit = Math.max(1, Math.min(limit, 20));

        // 최근 데이터 조회
        List<Cup> history = cupRepository.findByBinIdOrderByCreatedAtDesc(binId);

        // 최대 limit 개수만큼만 반환
        return history.stream()
            .limit(validLimit)
            .map(this::convertToHistoryDTO)
            .collect(Collectors.toList());
    }

    public CupStatsDTO getWeightStats(Long binId) {
        // 전체 개수
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

        // 액체 포함 타입들의 평균 무게
        List<CupWeightType> liquidTypes = Arrays.asList(
            CupWeightType.LIGHT_LIQUID,
            CupWeightType.MEDIUM_LIQUID,
            CupWeightType.HEAVY_LIQUID,
            CupWeightType.ABNORMAL
        );
        Double avgLiquidWeight = cupRepository.getAverageWeightByCupTypes(binId, liquidTypes);

        // 최대/최소 무게
        Double heaviestCup = cupRepository.getMaxWeight(binId);

        // 총 액체 무게
        Double totalLiquidWeight = cupRepository.getTotalLiquidWeightByCupTypes(binId, liquidTypes);

        // 액체 비율 계산
        double liquidRate = ((double) liquidCups / totalCups.doubleValue()) * 100;
        liquidRate = Math.round(liquidRate * 100.0) / 100.0; // 소수점 2자리

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
        if (request.getIsliquid() == null) {
            throw new GeneralException(CupErrorCode.INVALID_REQUEST);
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
            .isLiquid(cup.getIsLiquid())
            .cupType(cup.getCupType().name())
            .liquidWeight(cup.getLiquidWeight())
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
}
package com.smu.iot.domain.loadcell.service;

import com.smu.iot.domain.loadcell.dto.request.CupRequestDTO;
import com.smu.iot.domain.loadcell.dto.response.CupHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupResponseDTO;
import com.smu.iot.domain.loadcell.dto.response.CupStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CupService {

    // Mock: 무게 데이터 처리
    public CupResponseDTO processWeightData(CupRequestDTO request) {
        String uuid = UUID.randomUUID().toString();
        String cupType = determineCupType(request.getWeight(), request.getIsliquid());
        Double liquidWeight = request.getIsliquid() ? request.getWeight() - 5.5 : 0.0;

        return CupResponseDTO.builder()
            .recordId(System.currentTimeMillis())
            .uuid(uuid)
            .binId(1L)
            .weight(request.getWeight())
            .isLiquid(request.getIsliquid())
            .cupType(cupType)
            .liquidWeight(liquidWeight)
            .timestamp(LocalDateTime.now())
            .build();
    }

    // Mock: 무게 측정 이력 조회
    public List<CupHistoryDTO> getWeightHistory(Long binId, int limit) {
        List<CupHistoryDTO> history = new ArrayList<>();

        // Mock 데이터 생성
        for (int i = 0; i < Math.min(limit, 20); i++) {
            boolean isLiquid = i % 3 == 0; // 3개 중 1개는 액체 포함
            double weight = isLiquid ? 150.0 + (i * 10) : 5.0 + (i * 0.2);

            history.add(CupHistoryDTO.builder()
                .recordId((long) (1000 + i))
                .uuid(UUID.randomUUID().toString())
                .weight(weight)
                .isLiquid(isLiquid)
                .cupType(determineCupType(weight, isLiquid))
                .timestamp(LocalDateTime.now().minusMinutes(i * 5))
                .build());
        }

        return history;
    }

    // Mock: 무게 통계 조회
    public CupStatsDTO getWeightStats(Long binId) {
        return CupStatsDTO.builder()
            .totalCups(150)
            .emptyCups(110)
            .liquidCups(40)
            .liquidRate(26.67)
            .avgEmptyWeight(5.5)
            .avgLiquidWeight(175.3)
            .heaviestCup(280.5)
            .lightestCup(4.2)
            .weightRangeStats(
                CupStatsDTO.WeightRangeStats.builder()
                    .emptyCupCount(110)
                    .lightLiquidCount(15)
                    .mediumLiquidCount(20)
                    .heavyLiquidCount(5)
                    .build()
            )
            .build();
    }

    // Mock: UUID로 데이터 조회
    public CupResponseDTO getWeightByUuid(String uuid) {
        // Mock 데이터 반환
        return CupResponseDTO.builder()
            .recordId(12345L)
            .uuid(uuid)
            .binId(1L)
            .weight(5.5)
            .isLiquid(false)
            .cupType("EMPTY_CUP")
            .liquidWeight(0.0)
            .timestamp(LocalDateTime.now())
            .build();
    }

    // 무게로 컵 타입 결정
    private String determineCupType(Double weight, Boolean isLiquid) {
        if (!isLiquid && weight >= 4.0 && weight <= 7.0) {
            return "EMPTY_CUP";
        } else if (weight >= 20.0 && weight < 100.0) {
            return "LIGHT_LIQUID";
        } else if (weight >= 100.0 && weight < 200.0) {
            return "MEDIUM_LIQUID";
        } else if (weight >= 200.0 && weight <= 350.0) {
            return "HEAVY_LIQUID";
        }
        return "ABNORMAL";
    }
}
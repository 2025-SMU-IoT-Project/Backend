package com.smu.iot.domain.loadcell.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupStatsDTO {
    private Integer totalCups;
    private Integer emptyCups;
    private Integer liquidCups;
    private Double liquidRate;           // 액체 포함 비율 (%)
    private Double totalLiquidWeight;    // 총 액체 무게
    private Double avgLiquidWeight;      // 평균 액체 포함 무게
    private Double heaviestCup;          // 최대 무게

    // 무게 범위별 통계
    private WeightRangeStats weightRangeStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeightRangeStats {
        private Integer emptyCupCount;      // 우선 임의대로 설정함. 4-7g
        private Integer lightLiquidCount;   // 20-100g
        private Integer mediumLiquidCount;  // 100-200g
        private Integer heavyLiquidCount;   // 200-350g
        private Integer abnormalCount;      // 이외 무게
    }
}
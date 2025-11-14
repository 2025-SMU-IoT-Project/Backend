package com.smu.iot.domain.liquid.dto.response;

import com.smu.iot.domain.liquid.entitiy.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class LiquidResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateLiquidResultDTO {
        private Long id;
        private Long binId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidPreviewDTO {
        private Long id;
        private double weight;
        private LocalDateTime measuredAt;
        private Boolean overload;
        private Long binId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidPreviewListDTO {
        List<LiquidPreviewDTO> liquidPreviewDTOs;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidPreviewListWithAverageDTO {
        List<LiquidPreviewDTO> liquidPreviewDTOs;
        private double averageWeight;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidHistoryPreviewDTO {
        private Long id;
        private double weight;
        private LocalDateTime measuredAt;
        private Boolean overload;
        private Long binId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidTrendDTO {
        List<LiquidHistoryPreviewDTO> liquidHistoryPreviewDTOs;
        private PeriodType period;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidTotalTrendPointDTO {
        private String label; // 구간
        private double totalWeight; // 해당 구간(1일/1시간) 누적 무게 합
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LiquidTotalTrendDTO {
        private Long binId;
        private Long liquidId;
        private PeriodType period;
        private List<LiquidTotalTrendPointDTO> points;
        private double totalWeight; // 전체 누적 무게 합
    }
}

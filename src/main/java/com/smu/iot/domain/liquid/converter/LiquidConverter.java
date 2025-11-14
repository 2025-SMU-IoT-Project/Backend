package com.smu.iot.domain.liquid.converter;

import com.smu.iot.domain.liquid.dto.request.LiquidRequestDTO;
import com.smu.iot.domain.liquid.dto.response.LiquidResponseDTO;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.liquid.entitiy.PeriodType;
import com.smu.iot.domain.liquid.entitiy.TrendMode;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LiquidConverter {
    public static Liquid toLiquid(LiquidRequestDTO.CreateLiquidDTO createLiquidDTO) {
        return Liquid.builder()
                .weight(createLiquidDTO.getWeight())
                .build();
    }

    public static LiquidResponseDTO.CreateLiquidResultDTO toCreateLiquidResultDTO(Liquid liquid) {
        return LiquidResponseDTO.CreateLiquidResultDTO.builder()
                .id(liquid.getId())
                .binId(liquid.getBin().getId())
                .build();
    }

    public static LiquidResponseDTO.LiquidPreviewDTO toLiquidPreviewDTO(Liquid liquid) {
        return LiquidResponseDTO.LiquidPreviewDTO.builder()
                .id(liquid.getId())
                .weight(liquid.getWeight())
                .measuredAt(liquid.getMeasuredAt())
                .overload(liquid.getOverloaded())
                .binId(liquid.getBin().getId())
                .build();
    }

    public static LiquidResponseDTO.LiquidPreviewListDTO toLiquidPreviewListDTO(List<Liquid> liquids) {
        List<LiquidResponseDTO.LiquidPreviewDTO> liquidPreviewDTOs = liquids.stream()
                .map(LiquidConverter::toLiquidPreviewDTO).toList();

        return LiquidResponseDTO.LiquidPreviewListDTO.builder()
                .liquidPreviewDTOs(liquidPreviewDTOs)
                .build();
    }

    public static LiquidResponseDTO.LiquidPreviewListWithAverageDTO toLiquidPreviewListWithAverageDTO(List<Liquid> liquids, double averageWeight) {
        List<LiquidResponseDTO.LiquidPreviewDTO> liquidPreviewDTOs = liquids.stream()
                .map(LiquidConverter::toLiquidPreviewDTO).toList();

        return LiquidResponseDTO.LiquidPreviewListWithAverageDTO.builder()
                .liquidPreviewDTOs(liquidPreviewDTOs)
                .averageWeight(averageWeight)
                .build();
    }

    public static Object toLiquidTrendDTO(Long binId, List<LiquidHistory> histories, PeriodType period, TrendMode mode) {
        // mode TREND (기간동안 변화한 물통 무게 트렌드)
        if (mode == TrendMode.TREND) {
            List<LiquidResponseDTO.LiquidHistoryPreviewDTO> liquidHistoryPreviewDTOs = histories.stream()
                    .map(h -> LiquidResponseDTO.LiquidHistoryPreviewDTO.builder()
                            .id(h.getId())
                            .weight(h.getWeight())
                            .measuredAt(h.getMeasuredAt())
                            .overload(h.getOverload())
                            .binId(h.getBin().getId())
                            .build())
                    .toList();

            return LiquidResponseDTO.LiquidTrendDTO.builder()
                    .liquidHistoryPreviewDTOs(liquidHistoryPreviewDTOs)
                    .period(period)
                    .build();
        }

        // mode CUMULATIVE (기간 내 물통에 모인 액체 무게 누적합 트렌드)
        Map<String, Double> bucketTotals = new LinkedHashMap<>();
        double prevWeight = 0;

        for (LiquidHistory h : histories) {
            LocalDateTime measuredAt = h.getMeasuredAt();
            if (measuredAt == null) continue;

            double newWeight = h.getWeight();
            double delta = newWeight - prevWeight;

            if (delta < 0) delta = 0;

            String key = switch (period) {
                case MONTHLY, WEEKLY -> measuredAt.toLocalDate().toString(); // ex) 2025-11-12
                case DAILY -> String.format("%02d:00", measuredAt.getHour()); // ex) 13:00
            };

            if (delta > 0)
                bucketTotals.merge(key, delta, Double::sum);

            prevWeight = newWeight;
        }

        List<LiquidResponseDTO.LiquidTotalTrendPointDTO> points = bucketTotals.entrySet().stream()
                .map(e -> LiquidResponseDTO.LiquidTotalTrendPointDTO.builder()
                        .label(e.getKey())
                        .totalWeight(e.getValue())
                        .build())
                .toList();

        double totalWeight = bucketTotals.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Long liquidId = histories.isEmpty() ? null : histories.get(0).getId();

        return LiquidResponseDTO.LiquidTotalTrendDTO.builder()
                .binId(binId)
                .liquidId(liquidId)
                .period(period)
                .points(points)
                .totalWeight(totalWeight)
                .build();
    }
}
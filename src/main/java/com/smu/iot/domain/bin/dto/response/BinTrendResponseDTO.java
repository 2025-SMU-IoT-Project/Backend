package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinTrendResponseDTO {
    private Long binId;
    private String type;             // "CUP" or "LIQUID"
    private String period;           // "DAILY" or "MONTHLY"
    private String baseDate;         // 조회 기준 날짜
    private List<TrendPoint> trends; // 데이터 포인트 목록

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String label;        // X축: "09:00" (일간) or "11-19" (월간)
        private Double value;        // Y축: 투입 횟수 or 액체 무게(kg)
    }
}
package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinGlobalStatsDTO {
    private Long totalCups;          // 전체 컵 투입량
    private Double liquidRate;       // 액체 포함 비율 (%)
    private Long abnormalCount;      // 비정상 투입 횟수
    private Double averageFillRate;  // 전체 쓰레기통 평균 채움률 (%)
    private String period;           // 조회 기간 (DAILY, WEEKLY, MONTHLY)
}
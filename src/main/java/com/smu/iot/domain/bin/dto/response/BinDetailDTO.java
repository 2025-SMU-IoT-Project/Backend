package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinDetailDTO {
    private Long binId;
    private String binName;
    private Long totalCups;          // 컵 투입 횟수
    private Long abnormalCount;      // 비정상 투입 횟수
    private Double fillRate;         // 채움률 (%)
    private Double cupWeight;        // 컵통 무게 (kg)
    private Double liquidWeight;     // 물통 무게 (kg)
    private Double liquidRate;       // 액체 통 채움률 (%)
}
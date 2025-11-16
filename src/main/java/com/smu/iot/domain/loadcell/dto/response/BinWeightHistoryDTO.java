package com.smu.iot.domain.loadcell.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinWeightHistoryDTO {
    private Long id;
    private Long binId;
    private Double currentWeight;     // 측정 시점의 컵통 총 무게
    private Double addedWeight;       // 추가된 무게 (투입된 컵 무게)
    private String uuid;              // 연관 이벤트
    private LocalDateTime timestamp;  // 측정 시간
}
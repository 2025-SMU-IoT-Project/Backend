package com.smu.iot.domain.loadcell.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinWeightInitRequestDTO {
    private Long binId;         // 쓰레기통 ID
    private Double tareWeight;  // 빈 컵통의 무게 (g)
}
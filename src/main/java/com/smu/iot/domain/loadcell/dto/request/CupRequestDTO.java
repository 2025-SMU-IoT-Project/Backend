package com.smu.iot.domain.loadcell.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupRequestDTO {
    private String uuid;    // 이벤트 고유 ID
    private Long binId;     // 쓰레기통 ID
    private Double weight;  // 컵통의 현재 총 무게 (g)
    private String type;    // "CUP" or "WATER"
}
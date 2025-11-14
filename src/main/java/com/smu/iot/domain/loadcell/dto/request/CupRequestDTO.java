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

    private String uuid;        // 이벤트 고유 ID
    private Long binId;         // 쓰레기통 ID
    private Double weight;      // 측정된 무게 (g)
    private Boolean isliquid;   // 액체 포함 여부

    // STM32에서 계산해서 보낸다는 가정..
    private Double baseWeight;      // 영점 무게 (g)
    private Double weightThreshold; // 무게 감지 임계값
    private Double liquidThreshold; // 액체 판별 임계값 (기준 무게 + 10g → 20g 이상)
}
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
    private Double weight;      // 측정된 무게 (g)
    private Boolean isliquid;   // 액체 포함 여부
}
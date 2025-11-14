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
public class CupResponseDTO {
    private Long recordId;
    private String uuid;
    private Long binId;
    private Double weight;
    private Boolean isLiquid;
    private String cupType;          // EMPTY_CUP, LIGHT_LIQUID, MEDIUM_LIQUID, HEAVY_LIQUID
    private Double liquidWeight;     // 액체 무게
    private LocalDateTime timestamp;
}
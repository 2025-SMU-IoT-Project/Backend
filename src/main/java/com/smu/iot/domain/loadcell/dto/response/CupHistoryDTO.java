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
public class CupHistoryDTO {
    private Long recordId;
    private String uuid;
    private Double weight;
    private Boolean isLiquid;
    private String cupType;
    private LocalDateTime timestamp;
}
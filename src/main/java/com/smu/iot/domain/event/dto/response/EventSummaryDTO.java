package com.smu.iot.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSummaryDTO {
    private String uuid;
    private Long binId;
    private LocalDateTime timestamp;
    private Boolean isValidInput;
    private Boolean hasLiquid;
    private String cupType;
    private String cupPattern;
    private Boolean cupAccepted;
    private String description;
}
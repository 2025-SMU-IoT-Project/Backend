package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorHistoryResponseDTO {
    private LocalDateTime timestamp;
    private Double value;
}

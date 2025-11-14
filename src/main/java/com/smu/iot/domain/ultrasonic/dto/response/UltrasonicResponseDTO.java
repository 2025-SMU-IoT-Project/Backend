package com.smu.iot.domain.ultrasonic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UltrasonicResponseDTO {
    private Long binId;
    private String uuid;
    private Double distanceCm;
    private Double fillRate;
    private LocalDateTime createdAt;
}

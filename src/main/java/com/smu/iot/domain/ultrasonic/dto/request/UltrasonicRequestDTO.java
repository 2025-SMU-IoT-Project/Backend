package com.smu.iot.domain.ultrasonic.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UltrasonicRequestDTO {

    private Long binId;
    private String uuid;
    private Double distanceCm;
    private Double fillRate;
}

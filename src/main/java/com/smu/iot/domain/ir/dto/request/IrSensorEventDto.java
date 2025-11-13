package com.smu.iot.domain.ir.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrSensorEventDto {
    private String uuid;         // STM32에서 생성한 고유 ID
    private String sensorId;     // "IR1" or "IR2"
    private String binId;        // 쓰레기통 ID
    private Boolean beamBlocked; // IR 빔 차단 여부
}
package com.smu.iot.domain.ir.dto.response;

import com.smu.iot.domain.ir.entity.code.CupType;
import com.smu.iot.domain.ir.entity.code.InputStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorEventResponse {
    private Boolean success;
    private String message;
    private CupType detectedCupType;
    private InputStatus status;
    private Long eventId;  // 저장된 이벤트 ID
}
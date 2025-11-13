package com.smu.iot.domain.ir.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IrSensorErrorCode implements BaseErrorCode {

    EVENT_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "IR4040",
        "이벤트를 찾을 수 없습니다."
    ),
    UNKNOWN_SENSOR_ID(
        HttpStatus.BAD_REQUEST,
        "IR4010",
        "알 수 없는 센서 ID입니다."
    ),
    INVALID_EVENT_DATA(
        HttpStatus.BAD_REQUEST,
        "IR4020",
        "유효하지 않은 이벤트 데이터입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
package com.smu.iot.domain.ultrasonic.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UltrasonicErrorCode implements BaseErrorCode {

    BIN_NOT_FOUND(HttpStatus.NOT_FOUND,
        "ULTRASONIC_4001",
        "해당 쓰레기통을 찾을 수 없습니다."
    ),
    NO_DATA_FOUND(HttpStatus.NOT_FOUND,
        "ULTRASONIC_4002",
        "초음파 센서 데이터가 존재하지 않습니다."
    ),
    UUID_NOT_FOUND(HttpStatus.NOT_FOUND,
        "ULTRASONIC_4003",
        "해당 UUID의 데이터를 찾을 수 없습니다."
    ),
    INVALID_DISTANCE(HttpStatus.BAD_REQUEST,
        "ULTRASONIC_4004",
        "측정 거리 값이 유효하지 않습니다."
    ),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,
        "ULTRASONIC_4005",
        "유효하지 않은 요청입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
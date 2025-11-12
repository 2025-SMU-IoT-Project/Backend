package com.smu.iot.domain.laser.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LaserErrorCode implements BaseErrorCode {

    INVALID_BIN_WIDTH(
        HttpStatus.BAD_REQUEST,
        "LASER4001",
        "유효하지 않은 쓰레기통 너비입니다"
    ),
    INSUFFICIENT_SAMPLES(
        HttpStatus.BAD_REQUEST,
        "LASER4002",
        "샘플 수가 부족합니다"
    ),
    EVENT_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "LASER4040",
        "이벤트를 찾을 수 없습니다"
    ),
    PROCESSING_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "LASER5000",
        "이벤트 처리 중 오류가 발생했습니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
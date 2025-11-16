package com.smu.iot.domain.loadcell.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CupErrorCode implements BaseErrorCode {

    BIN_NOT_FOUND(HttpStatus.NOT_FOUND,
        "LOADCELL_4001",
        "해당 쓰레기통을 찾을 수 없습니다."
    ),
    NO_DATA_FOUND(HttpStatus.NOT_FOUND,
        "LOADCELL_4002",
        "로드셀 데이터가 존재하지 않습니다."
    ),
    UUID_NOT_FOUND(HttpStatus.NOT_FOUND,
        "LOADCELL_4003",
        "해당 UUID의 데이터를 찾을 수 없습니다."
    ),
    INVALID_WEIGHT(HttpStatus.BAD_REQUEST,
        "LOADCELL_4004",
        "측정 무게 값이 유효하지 않습니다."
    ),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST,
        "LOADCELL_4005",
        "유효하지 않은 요청입니다."
    ),
    BIN_NOT_INITIALIZED(HttpStatus.BAD_REQUEST,
        "LOADCELL_4006",
        "컵통이 초기화되지 않았습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
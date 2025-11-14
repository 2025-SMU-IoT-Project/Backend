package com.smu.iot.domain.bin.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BinErrorCode implements BaseErrorCode{
    // bin 에러
    _NOT_FOUND_BIN(HttpStatus.NOT_FOUND, "BIN400", "bin을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

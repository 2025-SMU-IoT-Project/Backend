package com.smu.iot.domain.liquid.code;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LiquidErrorCode implements BaseErrorCode {
    // liquid 에러
    _NOT_FOUND_LIQUID(HttpStatus.NOT_FOUND, "LIQUID400", "liquid를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

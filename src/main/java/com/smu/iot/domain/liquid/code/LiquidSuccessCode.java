package com.smu.iot.domain.liquid.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LiquidSuccessCode implements BaseSuccessCode {

    LIQUID_CREATED(HttpStatus.OK, "LIQUID2001", "물통이 성공적으로 생성되었습니다."),

    LIQUID_READ(HttpStatus.OK, "LIQUID2001", "물통이 성공적으로 조회되었습니다."),

    LIQUID_UPDATED(HttpStatus.OK, "LIQUID2001", "물통이 성공적으로 수정되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

package com.smu.iot.domain.bin.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BinSuccessCode implements BaseSuccessCode {

    BIN_CREATED(HttpStatus.OK, "BIN2001", "쓰레기통이 성공적으로 생성되었습니다."),

    BIN_READ(HttpStatus.OK, "BIN2001", "쓰레기통이 성공적으로 조회되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

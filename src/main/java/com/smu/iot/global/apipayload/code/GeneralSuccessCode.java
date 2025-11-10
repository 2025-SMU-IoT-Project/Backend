package com.smu.iot.global.apipayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GeneralSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK,
        "COMMON200",
        "요청에 성공했습니다"),
    CREATED(HttpStatus.CREATED,
        "COMMON201",
        "자원이 성공적으로 생성되었습니다"),
    NO_CONTENT(HttpStatus.NO_CONTENT,
        "COMMON204",
        "요청이 성공적으로 처리되었지만, 반환할 콘텐츠가 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
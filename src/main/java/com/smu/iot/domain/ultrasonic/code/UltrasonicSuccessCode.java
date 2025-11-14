package com.smu.iot.domain.ultrasonic.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UltrasonicSuccessCode implements BaseSuccessCode {

    ULTRASONIC_DATA_SAVED(HttpStatus.OK,
        "ULTRASONIC_2000",
        "초음파 센서 데이터가 성공적으로 저장되었습니다."
    ),
    FILL_RATE_RETRIEVED(HttpStatus.OK,
        "ULTRASONIC_2001",
        "채움률이 성공적으로 조회되었습니다."
    ),
    HISTORY_RETRIEVED(HttpStatus.OK,
        "ULTRASONIC_2002",
        "채움률 이력이 성공적으로 조회되었습니다."
    ),
    DATA_RETRIEVED(HttpStatus.OK,
        "ULTRASONIC_2003",
        "초음파 센서 데이터가 성공적으로 조회되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
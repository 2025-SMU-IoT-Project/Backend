package com.smu.iot.domain.ir.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IrSensorSuccessCode implements BaseSuccessCode {

    EVENT_PROCESSED(
        HttpStatus.OK,
        "IR2000",
        "센서 이벤트가 정상 처리되었습니다."
    ),
    EVENTS_RETRIEVED(
        HttpStatus.OK,
        "IR2010",
        "센서 이벤트 목록 조회 성공"
    ),
    RECORDS_RETRIEVED(
        HttpStatus.OK,
        "IR2020",
        "컵 투입 기록 조회 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
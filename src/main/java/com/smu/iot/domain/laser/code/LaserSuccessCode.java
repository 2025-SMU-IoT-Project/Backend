package com.smu.iot.domain.laser.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LaserSuccessCode implements BaseSuccessCode {

    VALID_CUP_DETECTED(
        HttpStatus.OK,
        "LASER2001",
        "유효한 일회용 컵이 감지되었습니다"
    ),
    INVALID_CUP_REJECTED(
        HttpStatus.OK,
        "LASER2002",
        "유효하지 않은 컵이 거부되었습니다"
    ),
    EVENT_DETAIL_RETRIEVED(
        HttpStatus.OK,
        "LASER2003",
        "이벤트 상세 정보를 조회했습니다"
    ),
    STATS_RETRIEVED(
        HttpStatus.OK,
        "LASER2004",
        "통계 정보를 조회했습니다"
    ),
    PACKET_RECEIVED(
        HttpStatus.OK,
        "LASER2005",
        "패킷이 정상적으로 수신되었습니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
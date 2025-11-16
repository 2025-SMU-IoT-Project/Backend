package com.smu.iot.domain.loadcell.code;

import com.smu.iot.global.apipayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CupSuccessCode implements BaseSuccessCode {

    WEIGHT_DATA_SAVED(HttpStatus.OK,
        "LOADCELL_2000",
        "무게 데이터가 성공적으로 처리되었습니다."
    ),
    HISTORY_RETRIEVED(HttpStatus.OK,
        "LOADCELL_2001",
        "무게 측정 이력이 조회되었습니다."
    ),
    STATS_RETRIEVED(HttpStatus.OK,
        "LOADCELL_2002",
        "통계가 조회되었습니다."
    ),
    DATA_RETRIEVED(HttpStatus.OK,
        "LOADCELL_2003",
        "로드셀 데이터가 조회되었습니다."
    ),
    BIN_INITIALIZED(HttpStatus.OK,
        "LOADCELL_2004",
        "컵통 무게가 초기화 되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
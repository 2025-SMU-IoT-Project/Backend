package com.smu.iot.global.apipayload.exception.handler;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;

public class LiquidHandler extends GeneralException {
    public LiquidHandler(BaseErrorCode code) {
        super(code);
    }
}

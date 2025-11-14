package com.smu.iot.global.apipayload.exception.handler;

import com.smu.iot.global.apipayload.code.BaseErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;

public class BinHandler extends GeneralException {
    public BinHandler(BaseErrorCode code) {
        super(code);
    }
}
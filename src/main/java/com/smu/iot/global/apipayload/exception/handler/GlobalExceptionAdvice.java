package com.smu.iot.global.apipayload.exception.handler;

import com.smu.iot.global.apipayload.ApiResponse;
import com.smu.iot.global.apipayload.code.BaseErrorCode;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(
        GeneralException ex
    ) {
        log.warn("[ CustomException ]: {}", ex.getCode().getMessage());
        ApiResponse<Object> errorResponse = ApiResponse.onFailure(ex.getCode(), null);
        return ResponseEntity
            .status(ex.getCode().getStatus())
            .body(errorResponse);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResponse<Object>> handleAllException(
        Exception ex
    ) {
        log.error("[WARNING] Internal Server Error : {} ", ex.getMessage());
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR_500;
        ApiResponse<Object> errorResponse = ApiResponse.onFailure(errorCode, null);
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(errorResponse);
    }
}
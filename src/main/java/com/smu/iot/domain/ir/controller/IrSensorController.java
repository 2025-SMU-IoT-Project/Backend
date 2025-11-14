package com.smu.iot.domain.ir.controller;

import com.smu.iot.domain.ir.code.IrSensorSuccessCode;
import com.smu.iot.domain.ir.dto.request.IrSensorEventDto;
import com.smu.iot.domain.ir.dto.response.SensorEventResponse;
import com.smu.iot.domain.ir.entity.Ir;
import com.smu.iot.domain.ir.service.IrSensorService;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sensor/ir")
@RequiredArgsConstructor
@Tag(name = "IR 센서", description = "IR 센서 이벤트 처리 및 컵 투입 관리 API")
public class IrSensorController {

    private final IrSensorService irSensorService;

    @PostMapping("/events")
    @Operation(
        summary = "IR 센서 이벤트 수신",
        description = "STM32 보드에서 전송한 IR 센서 데이터를 처리(IR1: 투입 감지, IR2: 컵 종류 판별)"
    )
    public ApiResponse<SensorEventResponse> receiveEvent(
        @RequestBody IrSensorEventDto eventDto) {
        SensorEventResponse response = irSensorService.processIrEvent(eventDto);
        return ApiResponse.onSuccess(IrSensorSuccessCode.EVENT_PROCESSED, response);
    }

    @GetMapping("/events/{binId}/recent")
    @Operation(
        summary = "최근 센서 이벤트 조회",
        description = "특정 쓰레기통의 최근 센서 이벤트를 조회"
    )
    public ApiResponse<List<Ir>> getRecentEvents(
        @PathVariable String binId,
        @RequestParam(defaultValue = "10") int limit) {
        List<Ir> events = irSensorService.getRecentEvents(binId, limit);
        return ApiResponse.onSuccess(IrSensorSuccessCode.EVENTS_RETRIEVED, events);
    }

    @GetMapping("/events/uuid/{uuid}")
    @Operation(
        summary = "UUID로 IR 센서값 조회",
        description = "하나의 UUID에 속한 IR 센서 데이터를 조회"
    )
    public ApiResponse<List<Ir>> getEventsByUuid(@PathVariable String uuid) {
        List<Ir> events = irSensorService.getEventsByUuid(uuid);
        return ApiResponse.onSuccess(IrSensorSuccessCode.EVENTS_RETRIEVED, events);
    }
}
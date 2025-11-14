package com.smu.iot.domain.ultrasonic.controller;

import com.smu.iot.domain.ultrasonic.dto.request.UltrasonicRequestDTO;
import com.smu.iot.domain.ultrasonic.dto.response.BinFillRateResponseDTO;
import com.smu.iot.domain.ultrasonic.dto.response.UltrasonicResponseDTO;
import com.smu.iot.domain.ultrasonic.service.UltrasonicService;
import com.smu.iot.global.apipayload.ApiResponse;
import com.smu.iot.global.apipayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sensor/ultrasonic")
@RequiredArgsConstructor
@Tag(name = "초음파 센서", description = "쓰레기통 채움률 측정 API")
public class UltrasonicController {

    private final UltrasonicService ultrasonicService;

    @PostMapping
    @Operation(
        summary = "초음파 센서 이벤트 수신",
        description = "STM32 보드에서 전송한 초음파 센서 데이터를 처리"
    )
    public ApiResponse<UltrasonicResponseDTO> receiveEvent(
        @RequestBody UltrasonicRequestDTO request) {

        log.info("Received ultrasonic data - binId: {}, distance: {}, fillRate: {}",
            request.getBinId(), request.getDistanceCm(), request.getFillRate());

        UltrasonicResponseDTO response = ultrasonicService.processUltrasonicData(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/fill/{binId}")
    @Operation(
        summary = "특정 쓰레기통의 채움률 조회",
        description = "binId를 입력하여 최근 채움률 조회"
    )
    public ApiResponse<BinFillRateResponseDTO> getFillRate(@PathVariable Long binId) {
        log.info("Querying fill rate - binId: {}", binId);

        BinFillRateResponseDTO response = ultrasonicService.getCurrentFillRate(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/history/{binId}")
    @Operation(
        summary = "채움률 이력 조회",
        description = "특정 쓰레기통의 채움률 변화 이력을 조회"
    )
    public ApiResponse<List<UltrasonicResponseDTO>> getFillRateHistory(
        @PathVariable Long binId,
        @RequestParam(defaultValue = "20") int limit) {

        log.info("Querying fill rate history - binId: {}, limit: {}", binId, limit);

        List<UltrasonicResponseDTO> history = ultrasonicService.getFillRateHistory(binId, limit);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, history);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(
        summary = "UUID로 초음파 센서 데이터 조회",
        description = "특정 UUID의 초음파 센서 측정 데이터를 조회"
    )
    public ApiResponse<UltrasonicResponseDTO> getDataByUuid(@PathVariable String uuid) {
        log.info("Querying ultrasonic data by UUID: {}", uuid);

        UltrasonicResponseDTO response = ultrasonicService.getDataByUuid(uuid);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
package com.smu.iot.domain.loadcell.controller;

import com.smu.iot.domain.loadcell.code.CupSuccessCode;
import com.smu.iot.domain.loadcell.dto.request.CupRequestDTO;
import com.smu.iot.domain.loadcell.dto.response.CupHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupResponseDTO;
import com.smu.iot.domain.loadcell.dto.response.CupStatsDTO;
import com.smu.iot.domain.loadcell.service.CupService;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sensors/cup")
@RequiredArgsConstructor
@Tag(name = "로드셀 센서(컵)", description = "컵 무게 측정 및 액체 감지 API")
public class CupController {

    private final CupService cupService;

    @PostMapping
    @Operation(
        summary = "컵 무게 및 액체 감지 데이터 수신",
        description = "STM32 보드에서 전송한 로드셀 센서 데이터를 처리"
    )
    public ApiResponse<CupResponseDTO> receiveWeightData(
        @RequestBody CupRequestDTO request) {

        log.info("Received loadcell data - weight: {}, isLiquid: {}",
            request.getWeight(), request.getIsliquid());

        CupResponseDTO response = cupService.processWeightData(request);
        return ApiResponse.onSuccess(CupSuccessCode.WEIGHT_DATA_SAVED, response);
    }

    @GetMapping("/history/{binId}")
    @Operation(
        summary = "무게 측정 이력 조회",
        description = "특정 쓰레기통의 최근 무게 측정 이력을 조회"
    )
    public ApiResponse<List<CupHistoryDTO>> getWeightHistory(
        @PathVariable Long binId,
        @RequestParam(defaultValue = "20") int limit) {

        log.info("Querying weight history - binId: {}, limit: {}", binId, limit);

        List<CupHistoryDTO> history = cupService.getWeightHistory(binId, limit);
        return ApiResponse.onSuccess(CupSuccessCode.HISTORY_RETRIEVED, history);
    }

    @GetMapping("/stats")
    @Operation(
        summary = "액체 포함 컵 통계 조회",
        description = "특정 쓰레기통의 컵 무게 및 액체 통계를 조회"
    )
    public ApiResponse<CupStatsDTO> getWeightStats(
        @RequestParam(defaultValue = "1") Long binId) {

        log.info("Querying weight stats - binId: {}", binId);

        CupStatsDTO stats = cupService.getWeightStats(binId);
        return ApiResponse.onSuccess(CupSuccessCode.STATS_RETRIEVED, stats);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(
        summary = "UUID로 로드셀 데이터 조회",
        description = "특정 UUID의 무게 측정 데이터를 조회"
    )
    public ApiResponse<CupResponseDTO> getWeightByUuid(@PathVariable String uuid) {
        log.info("Querying weight data by UUID: {}", uuid);

        CupResponseDTO response = cupService.getWeightByUuid(uuid);
        return ApiResponse.onSuccess(CupSuccessCode.DATA_RETRIEVED, response);
    }
}
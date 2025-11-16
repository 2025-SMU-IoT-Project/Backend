package com.smu.iot.domain.loadcell.controller;

import com.smu.iot.domain.loadcell.code.CupSuccessCode;
import com.smu.iot.domain.loadcell.dto.request.BinWeightInitRequestDTO;
import com.smu.iot.domain.loadcell.dto.request.CupRequestDTO;
import com.smu.iot.domain.loadcell.dto.response.BinWeightHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupHistoryDTO;
import com.smu.iot.domain.loadcell.dto.response.CupResponseDTO;
import com.smu.iot.domain.loadcell.dto.response.CupStatsDTO;
import com.smu.iot.domain.loadcell.service.CupService;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sensor/cup")
@RequiredArgsConstructor
@Tag(name = "로드셀 센서(컵)", description = "컵통 무게 측정 및 데이터 조회 API")
public class CupController {

    private final CupService cupService;

    @PostMapping("/init")
    @Operation(
        summary = "컵통 초기화",
        description = "빈 컵통의 무게를 설정하여 초기화합니다. 컵통 설치 시 최초 1회 호출 필요"
    )
    public ApiResponse<Void> initializeBinWeight(
        @RequestBody BinWeightInitRequestDTO request) {

        log.info("Initializing bin weight - binId: {}, tareWeight: {}g",
            request.getBinId(), request.getTareWeight());

        cupService.initializeBinWeight(request);
        return ApiResponse.onSuccess(CupSuccessCode.BIN_INITIALIZED, null);
    }

    @PostMapping
    @Operation(
        summary = "컵통 무게 데이터 수신",
        description = "STM32 보드에서 전송한 컵통의 무게 데이터를 처리하여 투입된 컵의 무게를 계산"
    )
    public ApiResponse<CupResponseDTO> receiveWeightData(
        @RequestBody CupRequestDTO request) {

        log.info("Received bin weight data - binId: {}, weight: {}g",
            request.getBinId(), request.getWeight());

        CupResponseDTO response = cupService.processWeightData(request);
        return ApiResponse.onSuccess(CupSuccessCode.WEIGHT_DATA_SAVED, response);
    }

    @PutMapping("/reset/{binId}")
    @Operation(
        summary = "컵통 무게 리셋",
        description = "컵통을 비웠을 때 무게를 영점(빈 컵통 무게)으로 초기화"
    )
    public ApiResponse<Void> resetBinWeight(@PathVariable Long binId) {
        log.info("Resetting bin weight - binId: {}", binId);

        cupService.resetBinWeight(binId);
        return ApiResponse.onSuccess(CupSuccessCode.BIN_INITIALIZED, null);
    }

    @GetMapping("/bin-weight/history/{binId}")
    @Operation(
        summary = "컵통 무게 히스토리 조회",
        description = "컵통 무게 변화 이력을 조회합니다. 그래프 그리기에 사용"
    )
    public ApiResponse<List<BinWeightHistoryDTO>> getBinWeightHistory(
        @PathVariable Long binId,
        @RequestParam(defaultValue = "100") int limit) {

        log.info("Querying bin weight history - binId: {}, limit: {}", binId, limit);

        List<BinWeightHistoryDTO> history = cupService.getBinWeightHistory(binId, limit);
        return ApiResponse.onSuccess(CupSuccessCode.HISTORY_RETRIEVED, history);
    }

    @GetMapping("/bin-weight/history/{binId}/range")
    @Operation(
        summary = "시간 범위별 컵통 무게 히스토리 조회",
        description = "특정 시간 범위의 컵통 무게 변화를 조회합니다"
    )
    public ApiResponse<List<BinWeightHistoryDTO>> getBinWeightHistoryByTimeRange(
        @PathVariable Long binId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        log.info("Querying bin weight history by time range - binId: {}, startTime: {}, endTime: {}",
            binId, startTime, endTime);

        List<BinWeightHistoryDTO> history = cupService.getBinWeightHistoryByTimeRange(binId, startTime, endTime);
        return ApiResponse.onSuccess(CupSuccessCode.HISTORY_RETRIEVED, history);
    }

    @GetMapping("/history/{binId}")
    @Operation(
        summary = "컵 무게 측정 이력 조회",
        description = "개별 컵의 무게 측정 이력을 조회"
    )
    public ApiResponse<List<CupHistoryDTO>> getWeightHistory(
        @PathVariable Long binId,
        @RequestParam(defaultValue = "20") int limit) {

        log.info("Querying cup weight history - binId: {}, limit: {}", binId, limit);

        List<CupHistoryDTO> history = cupService.getWeightHistory(binId, limit);
        return ApiResponse.onSuccess(CupSuccessCode.HISTORY_RETRIEVED, history);
    }

    @GetMapping("/stats")
    @Operation(
        summary = "통계 조회",
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
        summary = "UUID로 데이터 조회",
        description = "특정 UUID의 무게 측정 데이터를 조회"
    )
    public ApiResponse<CupResponseDTO> getWeightByUuid(@PathVariable String uuid) {
        log.info("Querying weight data by UUID: {}", uuid);

        CupResponseDTO response = cupService.getWeightByUuid(uuid);
        return ApiResponse.onSuccess(CupSuccessCode.DATA_RETRIEVED, response);
    }
}
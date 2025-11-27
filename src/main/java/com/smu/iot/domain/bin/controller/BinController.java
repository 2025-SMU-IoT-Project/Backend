package com.smu.iot.domain.bin.controller;

import com.smu.iot.domain.bin.dto.request.BinCreateRequestDTO;
import com.smu.iot.domain.bin.dto.request.BinUpdateRequestDTO;
import com.smu.iot.domain.bin.dto.response.*;
import com.smu.iot.domain.bin.service.BinService;
import com.smu.iot.domain.liquid.service.LiquidService;
import com.smu.iot.domain.loadcell.service.CupService;
import com.smu.iot.domain.ultrasonic.service.UltrasonicService;
import com.smu.iot.global.apipayload.ApiResponse;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.code.GeneralSuccessCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bin")
@RequiredArgsConstructor
@Tag(name = "쓰레기통", description = "쓰레기통 정보 조회 및 관리 API")
public class BinController {

    private final BinService binService;
    private final CupService cupService;
    private final LiquidService liquidService;
    private final UltrasonicService ultrasonicService;

    @GetMapping("/{binId}")
    @Operation(summary = "쓰레기통 기본 정보 조회", description = "특정 쓰레기통의 상세 정보를 조회")
    public ApiResponse<BinInfoDTO> getBinInfo(@PathVariable Long binId) {
        log.info("Querying bin detail - binId: {}", binId);

        BinInfoDTO detail = binService.getBinInfo(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, detail);
    }

    @GetMapping
    @Operation(summary = "전체 쓰레기통 목록 조회", description = "모든 쓰레기통의 목록을 조회 (상태 포함 옵션)")
    public ApiResponse<List<BinListDTO>> getAllBins(
        @RequestParam(defaultValue = "true") Boolean includeStatus) {

        log.info("Querying all bins - includeStatus: {}", includeStatus);

        List<BinListDTO> bins = binService.getAllBins(includeStatus);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, bins);
    }

    @PostMapping
    @Operation(summary = "쓰레기통 등록(Mock)", description = "새로운 쓰레기통을 등록")
    public ApiResponse<BinInfoDTO> createBin(@RequestBody BinCreateRequestDTO request) {
        log.info("Creating new bin - binCode: {}", request.getBinCode());

        BinInfoDTO result = binService.createBin(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    @PutMapping("/{binId}")
    @Operation(summary = "쓰레기통 정보 수정(Mock)", description = "기존 쓰레기통의 정보를 수정")
    public ApiResponse<BinInfoDTO> updateBin(
        @PathVariable Long binId,
        @RequestBody BinUpdateRequestDTO request) {

        log.info("Updating bin - binId: {}", binId);

        BinInfoDTO result = binService.updateBin(binId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/stats")
    @Operation(summary = "전체 쓰레기통 기간별 통계 조회", description = "모든 쓰레기통의 컵 투입량, 액체 비율, 비정상 투입, 평균 채움률 통계를 조회합니다. (기간: daily, weekly, monthly)")
    public ApiResponse<BinGlobalStatsDTO> getBinGlobalStats(
        @RequestParam(defaultValue = "daily") String period) {

        log.info("Querying global bin stats - period: {}", period);

        BinGlobalStatsDTO stats = binService.getBinGlobalStats(period);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, stats);
    }

    @GetMapping("detail/{binId}")
    @Operation(summary = "특정 쓰레기통 상세 조회", description = "특정 쓰레기통의 컵 투입 횟수, 비정상 투입 횟수, 현재 무게(kg), 액체 채움률 등을 조회합니다.")
    public ApiResponse<BinDetailDTO> getBinDetail(@PathVariable Long binId) {
        log.info("Querying detail stats for binId: {}", binId);
        BinDetailDTO stats = binService.getBinDetail(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, stats);
    }

    @GetMapping("/{binId}/trend/cup")
    @Operation(summary = "컵 투입 트렌드 조회", description = "특정 쓰레기통의 컵 투입 횟수 추이를 조회합니다. (옵션: 일간/월간)")
    public ApiResponse<BinTrendResponseDTO> getCupTrend(
        @PathVariable Long binId,
        @Parameter(description = "조회 기간 타입 (daily, monthly)") @RequestParam(defaultValue = "daily") String period,
        @Parameter(description = "조회 날짜 (yyyy-MM-dd 또는 yyyy-MM)") @RequestParam(required = false) String date) {

        log.info("Querying cup trend - binId: {}, period: {}, date: {}", binId, period, date);
        BinTrendResponseDTO result = binService.getCupTrend(binId, period, date);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/{binId}/trend/liquid")
    @Operation(summary = "액체 무게 변화량 트렌드 조회", description = "특정 쓰레기통의 액체 무게 증가량 추이를 조회합니다. (옵션: 일간/월간)")
    public ApiResponse<BinTrendResponseDTO> getLiquidTrend(
        @PathVariable Long binId,
        @Parameter(description = "조회 기간 타입 (daily, monthly)") @RequestParam(defaultValue = "daily") String period,
        @Parameter(description = "조회 날짜 (yyyy-MM-dd 또는 yyyy-MM)") @RequestParam(required = false) String date) {

        log.info("Querying liquid trend - binId: {}, period: {}, date: {}", binId, period, date);
        BinTrendResponseDTO result = binService.getLiquidTrend(binId, period, date);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/{binId}/sensor/{sensorType}/history/live")
    @Operation(summary = "실시간 센서 데이터 조회", description = "특정 센서의 실시간(LIVE) 데이터를 조회합니다. limit 파라미터가 있으면 리스트로, 없으면 최신 단일 객체로 반환합니다.")
    public ApiResponse<Object> getLiveSensorHistory(
        @PathVariable Long binId,
        @PathVariable String sensorType,
        @RequestParam(required = false) Integer limit) {

        log.info("Querying live sensor history - binId: {}, sensorType: {}, limit: {}", binId, sensorType, limit);

        Object result = null;
        switch (sensorType) {
            case "cup":
                result = cupService.getBinWeightHistory(binId, "LIVE", limit);
                break;
            case "liquid":
                result = liquidService.getLiquidHistory(binId, "LIVE", limit);
                break;
            case "ultrasonic":
                result = ultrasonicService.getUltrasonicHistory(binId, "LIVE", limit);
                break;
            default:
                throw new GeneralException(GeneralErrorCode.BAD_REQUEST_400);
        }

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }
}
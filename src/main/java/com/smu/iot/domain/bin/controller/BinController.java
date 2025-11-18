package com.smu.iot.domain.bin.controller;

import com.smu.iot.domain.bin.dto.request.BinCreateRequestDTO;
import com.smu.iot.domain.bin.dto.request.BinUpdateRequestDTO;
import com.smu.iot.domain.bin.dto.response.*;
import com.smu.iot.domain.bin.service.BinService;
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
@RequestMapping("/api/bin")
@RequiredArgsConstructor
@Tag(name = "쓰레기통", description = "쓰레기통 정보 조회 및 관리 API")
public class BinController {

    private final BinService binService;

    @GetMapping("/{binId}")
    @Operation(
        summary = "쓰레기통 기본 정보 조회",
        description = "특정 쓰레기통의 상세 정보를 조회"
    )
    public ApiResponse<BinInfoDTO> getBinInfo(@PathVariable Long binId) {
        log.info("Querying bin detail - binId: {}", binId);

        BinInfoDTO detail = binService.getBinInfo(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, detail);
    }

    @GetMapping("/{binId}/status")
    @Operation(
        summary = "쓰레기통 현재 상태 조회",
        description = "쓰레기통의 실시간 상태 정보를 조회 (채움률, 센서 상태 등)"
    )
    public ApiResponse<BinStatusDTO> getBinStatus(@PathVariable Long binId) {
        log.info("Querying bin status - binId: {}", binId);

        BinStatusDTO status = binService.getBinStatus(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, status);
    }

    @GetMapping
    @Operation(
        summary = "전체 쓰레기통 목록 조회",
        description = "모든 쓰레기통의 목록을 조회 (상태 포함 옵션)"
    )
    public ApiResponse<List<BinListDTO>> getAllBins(
        @RequestParam(defaultValue = "true") Boolean includeStatus) {
        
        log.info("Querying all bins - includeStatus: {}", includeStatus);

        List<BinListDTO> bins = binService.getAllBins(includeStatus);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, bins);
    }

    @GetMapping("/needs-collection")
    @Operation(
        summary = "수거가 필요한 쓰레기통 목록 조회",
        description = "채움률이 임계값을 초과한 쓰레기통 목록을 조회"
    )
    public ApiResponse<BinCollectionDTO> getBinsNeedingCollection(
        @RequestParam(defaultValue = "80") Double threshold) {
        
        log.info("Querying bins needing collection - threshold: {}", threshold);

        BinCollectionDTO result = binService.getBinsNeedingCollection(threshold);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @PostMapping
    @Operation(
        summary = "쓰레기통 등록",
        description = "새로운 쓰레기통을 등록"
    )
    public ApiResponse<BinInfoDTO> createBin(@RequestBody BinCreateRequestDTO request) {
        log.info("Creating new bin - binCode: {}", request.getBinCode());

        BinInfoDTO result = binService.createBin(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    @PutMapping("/{binId}")
    @Operation(
        summary = "쓰레기통 정보 수정",
        description = "기존 쓰레기통의 정보를 수정"
    )
    public ApiResponse<BinInfoDTO> updateBin(
        @PathVariable Long binId,
        @RequestBody BinUpdateRequestDTO request) {
        
        log.info("Updating bin - binId: {}", binId);

        BinInfoDTO result = binService.updateBin(binId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/stats")
    @Operation(
        summary = "전체 쓰레기통 기간별 통계 조회",
        description = "모든 쓰레기통의 컵 투입량, 액체 비율, 비정상 투입, 평균 채움률 통계를 조회합니다. (기간: daily, weekly, monthly)"
    )
    public ApiResponse<BinGlobalStatsDTO> getBinGlobalStats(
        @RequestParam(defaultValue = "daily") String period) {

        log.info("Querying global bin stats - period: {}", period);

        BinGlobalStatsDTO stats = binService.getBinGlobalStats(period);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, stats);
    }

    @GetMapping("detail/{binId}")
    @Operation(
        summary = "특정 쓰레기통 상세 조회",
        description = "특정 쓰레기통의 컵 투입 횟수, 비정상 투입 횟수, 현재 무게(kg), 액체 채움률 등을 조회합니다."
    )
    public ApiResponse<BinDetailDTO> getBinDetail(@PathVariable Long binId) {
        log.info("Querying detail stats for binId: {}", binId);
        BinDetailDTO stats = binService.getBinDetail(binId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, stats);
    }
}
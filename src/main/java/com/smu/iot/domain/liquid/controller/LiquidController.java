package com.smu.iot.domain.liquid.controller;

import com.smu.iot.domain.liquid.code.LiquidSuccessCode;
import com.smu.iot.domain.liquid.converter.LiquidConverter;
import com.smu.iot.domain.liquid.dto.request.LiquidRequestDTO;
import com.smu.iot.domain.liquid.dto.response.LiquidResponseDTO;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import com.smu.iot.domain.liquid.service.LiquidService;
import com.smu.iot.domain.liquid.entitiy.PeriodType;
import com.smu.iot.domain.liquid.entitiy.TrendMode;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sensors/weight/liquids")
@Tag(name = "무게 센서", description = "물통 로드셀 + HX711 센서 API")
public class LiquidController {
    private final LiquidService liquidService;

    @PostMapping("/by-bin/{binId}")
    @Operation(
            summary = "물통 생성",
            description = "binId를 받아 물통을 생성"
    )
    public ApiResponse<LiquidResponseDTO.CreateLiquidResultDTO> createLiquid(@PathVariable Long binId, @RequestBody LiquidRequestDTO.CreateLiquidDTO createLiquidDTO) {
        Liquid liquid = liquidService.createLiquid(binId, createLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_CREATED, LiquidConverter.toCreateLiquidResultDTO(liquid));
    }

    @GetMapping("/by-bin/{binId}")
    @Operation(
            summary = "특정 물통 조회 (by binId)",
            description = "binId로 특정 물통을 조회"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> readLiquidByBinId(@PathVariable Long binId) {
        Liquid liquid = liquidService.readLiquidByBinId(binId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    @GetMapping("/{liquidId}")
    @Operation(
            summary = "특정 물통 조회 (by liquidId)",
            description = "liquidId로 특정 물통을 조회"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> readLiquidById(@PathVariable Long liquidId) {
        Liquid liquid = liquidService.readLiquidById(liquidId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    @GetMapping
    @Operation(
            summary = "전체 물통 조회",
            description = "전체 liquid 정보와 전체 무게 평균 조회"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewListWithAverageDTO> readLiquids() {
        LiquidResponseDTO.LiquidPreviewListWithAverageDTO liquidsWithAverage = liquidService.readLiquids();
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidsWithAverage);
    }

    @PatchMapping("/by-bin/{binId}")
    @Operation(
            summary = "특정 물통 무게 수정(by binId)",
            description = "binId와 무게를 받아 특정 물통 무게를 수정"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> updateLiquidByBinId(@PathVariable Long binId, @RequestBody LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidService.updateLiquidByBinId(binId, updateLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    @PatchMapping("/{liquidId}")
    @Operation(
            summary = "특정 물통 무게 수정(by liquidId)",
            description = "liquidId와 무게를 받아 특정 물통 무게를 수정"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> updateLiquidById(@PathVariable Long liquidId, @RequestBody LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidService.updateLiquidById(liquidId, updateLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    @GetMapping("/overload/{liquidId}")
    @Operation(
            summary = "특정 물통 과적 조회",
            description = "liquidId로 특정 물통의 무게가 4kg 이상 찼는지 여부를 조회"
    )
    public ApiResponse<Boolean> isLiquidOverloadedById(@PathVariable Long liquidId) {
        Boolean overloaded = liquidService.isLiquidOverloadedById(liquidId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, overloaded);
    }

    @GetMapping("/overload")
    @Operation(
            summary = "전채 물통 과적 조회",
            description = "4kg 이상 찬 물통의 리스트를 조회"
    )
    public ApiResponse<LiquidResponseDTO.LiquidPreviewListDTO> readLiquidsOverloadedById() {
        LiquidResponseDTO.LiquidPreviewListDTO liquidPreviewList = liquidService.readLiquidsOverloaded();
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidPreviewList);
    }


    @GetMapping("/by-bin/{binId}/trend")
    @Operation(
            summary = "특정 물통 트렌드 조회 (by binId)",
            description = "binId로 특정 물통 트렌드(시간 구간대별 무게/누적합) 조회 (월별, 주별, 일별)"
    )
    public ApiResponse<Object> readLiquidTrendByBinID(@PathVariable Long binId, @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
                                                                                 @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                                 @RequestParam(name = "mode", defaultValue = "TREND") TrendMode mode) {
        Object liquidTrend = liquidService.readLiquidTrendByBinId(binId, period, date, mode);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidTrend);
    }

    @GetMapping("{liquidId}/trend")
    @Operation(
            summary = "특정 물통 트렌드 조회 (by liquidId)",
            description = "liquidId로 특정 물통 트렌드(시간 구간대별 무게/누적합) 조회 (월별, 주별, 일별)"
    )
    public ApiResponse<Object> readLiquidTrendByID(@PathVariable Long liquidId, @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
                                                       @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                       @RequestParam(name = "mode", defaultValue = "TREND") TrendMode mode) {
        Object liquidTrend = liquidService.readLiquidTrendById(liquidId, period, date, mode);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidTrend);
    }
}

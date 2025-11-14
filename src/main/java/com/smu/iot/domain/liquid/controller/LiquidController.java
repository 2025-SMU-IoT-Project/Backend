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

    // 물통 생성
    // 쓰레기통 생성 시 반드시 물통 생성
    @PostMapping("/by-bin/{binId}")
    public ApiResponse<LiquidResponseDTO.CreateLiquidResultDTO> createLiquid(@PathVariable Long binId, @RequestBody LiquidRequestDTO.CreateLiquidDTO createLiquidDTO) {
        Liquid liquid = liquidService.createLiquid(binId, createLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_CREATED, LiquidConverter.toCreateLiquidResultDTO(liquid));
    }

    // 특정 물통 정보 조회 (by binId)
    @GetMapping("/by-bin/{binId}")
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> readLiquidByBinId(@PathVariable Long binId) {
        Liquid liquid = liquidService.readLiquidByBinId(binId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    // 특정 물통 정보 조회 (by liquidId)
    @GetMapping("/{liquidId}")
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> readLiquidById(@PathVariable Long liquidId) {
        Liquid liquid = liquidService.readLiquidById(liquidId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    // 전체 liquid 정보 + 전체 무게 평균 조회
    @GetMapping
    public ApiResponse<LiquidResponseDTO.LiquidPreviewListWithAverageDTO> readLiquids() {
        LiquidResponseDTO.LiquidPreviewListWithAverageDTO liquidsWithAverage = liquidService.readLiquids();
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidsWithAverage);
    }

    // 특정 물통 무게 수정 (by binId)
    @PatchMapping("/by-bin/{binId}")
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> updateLiquidByBinId(@PathVariable Long binId, @RequestBody LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidService.updateLiquidByBinId(binId, updateLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    // 특정 물통 무게 수정 (by liquidId)
    @PatchMapping("/{liquidId}")
    public ApiResponse<LiquidResponseDTO.LiquidPreviewDTO> updateLiquidById(@PathVariable Long liquidId, @RequestBody LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidService.updateLiquidById(liquidId, updateLiquidDTO);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, LiquidConverter.toLiquidPreviewDTO(liquid));
    }

    // 특정 물통 무게 4kg 이상(80% 이상) 찼는지 여부 조회
    @GetMapping("/overload/{liquidId}")
    public ApiResponse<Boolean> isLiquidOverloadedById(@PathVariable Long liquidId) {
        Boolean overloaded = liquidService.isLiquidOverloadedById(liquidId);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, overloaded);
    }

    // 물통 무게 4kg 이상(80% 이상) 찬 물통 전체 조회
    @GetMapping("/overload")
    public ApiResponse<LiquidResponseDTO.LiquidPreviewListDTO> readLiquidsOverloadedById() {
        LiquidResponseDTO.LiquidPreviewListDTO liquidPreviewList = liquidService.readLiquidsOverloaded();
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidPreviewList);
    }

    // 특정 물통 트렌드(시간 구간대별 무게/누적합) 조회 (by binId) (월별, 주별, 일별)
    @GetMapping("/by-bin/{binId}/trend")
    public ApiResponse<Object> readLiquidTrendByBinID(@PathVariable Long binId, @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
                                                                                 @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                                 @RequestParam(name = "mode", defaultValue = "TREND") TrendMode mode) {
        Object liquidTrend = liquidService.readLiquidTrendByBinId(binId, period, date, mode);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidTrend);
    }

    // 특정 물통 트렌드(시간 구간대별 무게/누적합) 조회 (by liquidId) (월별, 주별, 일별)
    @GetMapping("{liquidId}/trend")
    public ApiResponse<Object> readLiquidTrendByID(@PathVariable Long liquidId, @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
                                                       @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                       @RequestParam(name = "mode", defaultValue = "TREND") TrendMode mode) {
        Object liquidTrend = liquidService.readLiquidTrendById(liquidId, period, date, mode);
        return ApiResponse.onSuccess(LiquidSuccessCode.LIQUID_READ, liquidTrend);
    }
}

package com.smu.iot.domain.bin.controller;

import com.smu.iot.domain.bin.code.BinSuccessCode;
import com.smu.iot.domain.bin.converter.BinConverter;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.dto.request.BinRequestDTO;
import com.smu.iot.domain.bin.dto.response.BinResponseDTO;
import com.smu.iot.domain.bin.service.BinService;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bin")
@Tag(name = "쓰레기통 API", description = "쓰레기통 생성 및 조회")
public class BinController {
    private final BinService binService;

    @PostMapping
    @Operation(
            summary = "물통 생성",
            description = "물통 하나를 생성"
    )
    public ApiResponse<BinResponseDTO.CreateBinResultDTO> createBin(@RequestBody BinRequestDTO.CreateBinDTO createBinDTO) {
        Bin bin = binService.createBin(createBinDTO);
        return ApiResponse.onSuccess(BinSuccessCode.BIN_CREATED, BinConverter.tocreateBinResultDTO(bin));
    }

    @GetMapping("/{binId}")
    @Operation(
            summary = "물통 조회",
            description = "binId를 받아 물통을 조회"
    )
    public ApiResponse<BinResponseDTO.BinPreviewDTO> readBin(Long binId) {
        Bin bin = binService.readBin(binId);
        return ApiResponse.onSuccess(BinSuccessCode.BIN_READ, BinConverter.toBinPreviewDTO(bin));
    }
}
package com.smu.iot.domain.bin.controller;

import com.smu.iot.domain.bin.code.BinSuccessCode;
import com.smu.iot.domain.bin.converter.BinConverter;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.dto.request.BinRequestDTO;
import com.smu.iot.domain.bin.dto.response.BinResponseDTO;
import com.smu.iot.domain.bin.service.BinService;
import com.smu.iot.global.apipayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bin")
public class BinController {
    private final BinService binService;

    @PostMapping
    public ApiResponse<BinResponseDTO.CreateBinResultDTO> createBin(@RequestBody BinRequestDTO.CreateBinDTO createBinDTO) {
        Bin bin = binService.createBin(createBinDTO);
        return ApiResponse.onSuccess(BinSuccessCode.BIN_CREATED, BinConverter.tocreateBinResultDTO(bin));
    }

    @GetMapping("/{binId}")
    public ApiResponse<BinResponseDTO.BinPreviewDTO> readBin(Long binId) {
        Bin bin = binService.readBin(binId);
        return ApiResponse.onSuccess(BinSuccessCode.BIN_READ, BinConverter.toBinPreviewDTO(bin));
    }
}
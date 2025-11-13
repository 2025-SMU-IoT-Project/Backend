package com.example.IoTBack.trashBin.bin.controller;

import com.example.IoTBack.global.apiPayload.ApiResponse;
import com.example.IoTBack.trashBin.bin.converter.BinConverter;
import com.example.IoTBack.trashBin.bin.domain.Bin;
import com.example.IoTBack.trashBin.bin.dto.request.BinRequestDTO;
import com.example.IoTBack.trashBin.bin.dto.response.BinResponseDTO;
import com.example.IoTBack.trashBin.bin.service.BinService;
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
        return ApiResponse.onSuccess(BinConverter.tocreateBinResultDTO(bin));
    }

    @GetMapping("/{binId}")
    public ApiResponse<BinResponseDTO.BinPreviewDTO> readBin(Long binId) {
        Bin bin = binService.readBin(binId);
        return ApiResponse.onSuccess(BinConverter.toBinPreviewDTO(bin));
    }
}
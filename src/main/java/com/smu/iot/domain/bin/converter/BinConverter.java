//package com.smu.iot.domain.bin.converter;
//
//import com.smu.iot.domain.bin.entity.Bin;
//import com.smu.iot.domain.bin.dto.request.BinRequestDTO;
//import com.smu.iot.domain.bin.dto.response.BinResponseDTO;
//
//public class BinConverter {
//    public static Bin toBin(BinRequestDTO.CreateBinDTO createBinDTO) {
//        return Bin.builder()
//                .name(createBinDTO.getName())
//                .location(createBinDTO.getLocation())
//                .build();
//    }
//
//    public static BinResponseDTO.CreateBinResultDTO tocreateBinResultDTO(Bin bin) {
//        return BinResponseDTO.CreateBinResultDTO.builder()
//                .id(bin.getId())
//                .name(bin.getName())
//                .location(bin.getLocation())
//                .build();
//    }
//
//    public static BinResponseDTO.BinPreviewDTO toBinPreviewDTO(Bin bin) {
//        return BinResponseDTO.BinPreviewDTO.builder()
//                .id(bin.getId())
//                .name(bin.getName())
//                .location(bin.getLocation())
//                .build();
//    }
//}

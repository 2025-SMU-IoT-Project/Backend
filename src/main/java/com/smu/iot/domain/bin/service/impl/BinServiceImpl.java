//package com.smu.iot.domain.bin.service.impl;
//
//import com.smu.iot.domain.bin.code.BinErrorCode;
//import com.smu.iot.domain.bin.converter.BinConverter;
//import com.smu.iot.domain.bin.entity.Bin;
//import com.smu.iot.domain.bin.dto.request.BinRequestDTO;
//import com.smu.iot.domain.bin.repository.BinRepository;
//import com.smu.iot.domain.bin.service.BinService;
//import com.smu.iot.global.apipayload.exception.handler.BinHandler;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class BinServiceImpl implements BinService {
//    private final BinRepository binRepository;
//
//    @Override
//    public Bin createBin(BinRequestDTO.CreateBinDTO createBinDTO) {
//        Bin bin = BinConverter.toBin(createBinDTO);
//        return binRepository.save(bin);
//    }
//
//    @Transactional(readOnly = true)
//    @Override
//    public Bin readBin(@PathVariable Long binId) {
//        return binRepository.findById(binId).orElseThrow(() -> {
//            throw new BinHandler(BinErrorCode._NOT_FOUND_BIN);
//        });
//    }
//}

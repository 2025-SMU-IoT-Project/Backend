package com.smu.iot.domain.bin.service;

import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.dto.request.BinRequestDTO;

public interface BinService {
    Bin createBin(BinRequestDTO.CreateBinDTO createBinDTO);

    Bin readBin(Long binId);
}

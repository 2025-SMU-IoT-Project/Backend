package com.smu.iot.domain.liquid.service;

import com.smu.iot.domain.liquid.dto.request.LiquidRequestDTO;
import com.smu.iot.domain.liquid.dto.response.LiquidResponseDTO;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import com.smu.iot.domain.liquid.entitiy.PeriodType;
import com.smu.iot.domain.liquid.entitiy.TrendMode;

import java.time.LocalDate;

public interface LiquidService {

    Liquid createLiquid(Long binId, LiquidRequestDTO.CreateLiquidDTO createLiquidDTO);

    Liquid readLiquidByBinId(Long binId);

    Liquid readLiquidById(Long liquidId);

    LiquidResponseDTO.LiquidPreviewListWithAverageDTO readLiquids();

    Liquid updateLiquidByBinId(Long binId, LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO);

    Liquid updateLiquidById(Long liquidId, LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO);

    Boolean isLiquidOverloadedById(Long liquidId);

    LiquidResponseDTO.LiquidPreviewListDTO readLiquidsOverloaded();

    Object readLiquidTrendByBinId(Long binId, PeriodType period, LocalDate date, TrendMode mode);

    Object readLiquidTrendById(Long liquidId, PeriodType period, LocalDate date, TrendMode mode);
}

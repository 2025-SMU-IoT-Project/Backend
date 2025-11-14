package com.smu.iot.domain.liquid.service.impl;

import com.smu.iot.domain.bin.code.BinErrorCode;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.liquid.code.LiquidErrorCode;
import com.smu.iot.domain.liquid.converter.LiquidConverter;
import com.smu.iot.domain.liquid.dto.request.LiquidRequestDTO;
import com.smu.iot.domain.liquid.dto.response.LiquidResponseDTO;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.liquid.repository.LiquidHistoryRepository;
import com.smu.iot.domain.liquid.repository.LiquidRepository;
import com.smu.iot.domain.liquid.service.LiquidService;
import com.smu.iot.domain.liquid.entitiy.PeriodType;
import com.smu.iot.domain.liquid.entitiy.TrendMode;
import com.smu.iot.global.apipayload.exception.handler.BinHandler;
import com.smu.iot.global.apipayload.exception.handler.LiquidHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LiquidServiceImpl implements LiquidService {
    private final LiquidRepository liquidRepository;
    private final BinRepository binRepository;
    private final LiquidHistoryRepository liquidHistoryRepository;

    @Override
    public Liquid createLiquid(Long binId, LiquidRequestDTO.CreateLiquidDTO createLiquidDTO) {
        Liquid liquid = LiquidConverter.toLiquid(createLiquidDTO);
        Bin bin = binRepository.findById(binId).orElseThrow(() -> {
            throw new BinHandler(BinErrorCode._NOT_FOUND_BIN);
        });
        liquid.setBin(bin);
        return liquidRepository.save(liquid);
    }

    @Transactional(readOnly = true)
    @Override
    public Liquid readLiquidByBinId(Long binId) {
        Bin bin = binRepository.findById(binId).orElseThrow(() -> {
            throw new BinHandler(BinErrorCode._NOT_FOUND_BIN);
        });

        return liquidRepository.findByBin(bin).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Liquid readLiquidById(Long liquidId) {
        return liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public LiquidResponseDTO.LiquidPreviewListWithAverageDTO readLiquids() {
        List<Liquid> liquids = liquidRepository.findAll();

        List<LiquidResponseDTO.LiquidPreviewDTO> items = liquids.stream()
                .map(LiquidConverter::toLiquidPreviewDTO)
                .toList();

        // 전체 물통 평균 무게 계산
        double averageWeight = items.isEmpty() ? 0.0 :
                items.stream()
                        .mapToDouble(LiquidResponseDTO.LiquidPreviewDTO::getWeight)
                        .average()
                        .orElse(0.0);

        return LiquidConverter.toLiquidPreviewListWithAverageDTO(liquids, averageWeight);
    }

    @Override
    public Liquid updateLiquidByBinId(Long binId, LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Bin bin = binRepository.findById(binId).orElseThrow(() -> {
            throw new BinHandler(BinErrorCode._NOT_FOUND_BIN);
        });

        Liquid liquid = liquidRepository.findByBin(bin).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        // weight, addedWeight 업데이트
        updateLiquidWeight(liquid, updateLiquidDTO.getWeight());

        // LiquidHistory 기록 추가
        addLiquidHistory(liquid);

        return liquid;
    }

    @Override
    public Liquid updateLiquidById(Long liquidId, LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        // weight, addedWeight 업데이트
        updateLiquidWeight(liquid, updateLiquidDTO.getWeight());

        // LiquidHistory 기록 추가
        addLiquidHistory(liquid);

        return liquid;
    }

    @Override
    public Boolean isLiquidOverloadedById(Long liquidId) {
        Liquid liquid = liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        return liquid.getOverloaded();
    }

    @Override
    public LiquidResponseDTO.LiquidPreviewListDTO readLiquidsOverloaded() {
        List<Liquid> liquids = liquidRepository.findAllByOverloaded(true);
        return LiquidConverter.toLiquidPreviewListDTO(liquids);
    }

    @Override
    public Object readLiquidTrendByBinId(Long binId, PeriodType period, LocalDate date, TrendMode mode) {
        binRepository.findById(binId).orElseThrow(() -> {
            throw new BinHandler(BinErrorCode._NOT_FOUND_BIN);
        });

        // period에 맞는 liquid 리스트 찾기
        List<LiquidHistory> histories = findLiquidsByDate(binId, period, date);

        // Converter에서 mode에 따라 변환
        return LiquidConverter.toLiquidTrendDTO(binId, histories, period, mode);
    }

    @Override
    public Object readLiquidTrendById(Long liquidId, PeriodType period, LocalDate date, TrendMode mode) {
        Liquid liquid = liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        Long binId = liquid.getBin().getId();
        // period에 맞는 liquid 리스트 찾기
        List<LiquidHistory> histories = findLiquidsByDate(binId, period, date);

        // Converter에서 mode에 따라 변환
        return LiquidConverter.toLiquidTrendDTO(binId, histories, period, mode);
    }

    public void updateLiquidWeight(Liquid liquid, double newWeight) {
        // addedWeight 업데이트
        double oldWeight = liquid.getWeight();
        double addedWeight = (newWeight > oldWeight) ? newWeight-oldWeight: 0;

        // overloaded 업데이트
        Boolean overloaded = (newWeight >= 4000);

        // liquid 업데이트
        liquid.update(newWeight, addedWeight, overloaded, LocalDateTime.now());
    }

    public void addLiquidHistory(Liquid liquid) {
        LiquidHistory history = LiquidHistory.builder()
                .bin(liquid.getBin())
                .liquid(liquid)
                .weight(liquid.getWeight())
                .addedWeight(liquid.getAddedWeight())
                .overload(liquid.getOverloaded())
                .measuredAt(LocalDateTime.now())
                .build();

        liquidHistoryRepository.save(history);
    }

    public List<LiquidHistory> findLiquidsByDate(Long binId, PeriodType period, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        // period에 따라 조회 기간(start, end) 계산
        LocalDateTime start;
        LocalDateTime end;

        switch (period) {
            case MONTHLY -> {
                LocalDate firstDay = date.withDayOfMonth(1);
                start = firstDay.atStartOfDay();
                end = firstDay.plusMonths(1).atStartOfDay();
            }
            case WEEKLY -> {
                WeekFields wf = WeekFields.ISO;
                LocalDate firstDayOfWeek = date.with(wf.dayOfWeek(), 1); // 월요일 기준
                start = firstDayOfWeek.atStartOfDay();
                end = firstDayOfWeek.plusWeeks(1).atStartOfDay();
            }
            case DAILY -> {
                start = date.atStartOfDay();
                end = start.plusDays(1);
            }
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        }

        // 기간 내 LiquidHistory 조회
        return liquidHistoryRepository.findByBinIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(binId, start, end);
    }
}


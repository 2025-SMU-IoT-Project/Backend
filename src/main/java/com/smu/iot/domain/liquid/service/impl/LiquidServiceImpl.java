package com.smu.iot.domain.liquid.service.impl;

import com.smu.iot.domain.bin.code.BinErrorCode;
import com.smu.iot.domain.bin.dto.response.SensorHistoryResponseDTO;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.event.service.EventService;
import com.smu.iot.domain.liquid.code.LiquidErrorCode;
import com.smu.iot.domain.liquid.converter.LiquidConverter;
import com.smu.iot.domain.liquid.dto.request.LiquidRequestDTO;
import com.smu.iot.domain.liquid.dto.response.LiquidResponseDTO;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.liquid.entitiy.PeriodType;
import com.smu.iot.domain.liquid.entitiy.TrendMode;
import com.smu.iot.domain.liquid.repository.LiquidHistoryRepository;
import com.smu.iot.domain.liquid.repository.LiquidRepository;
import com.smu.iot.domain.liquid.service.LiquidService;
import com.smu.iot.global.apipayload.exception.handler.BinHandler;
import com.smu.iot.global.apipayload.exception.handler.LiquidHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LiquidServiceImpl implements LiquidService {
    private final LiquidRepository liquidRepository;
    private final BinRepository binRepository;
    private final LiquidHistoryRepository liquidHistoryRepository;
    private final EventService eventService;

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

        double averageWeight = items.isEmpty() ? 0.0
            : items.stream()
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
        updateLiquidWeight(liquid, updateLiquidDTO.getWeight(), updateLiquidDTO.getUuid());

        // LiquidHistory 기록 추가
        LiquidHistory liquidHistory = addLiquidHistory(liquid, updateLiquidDTO.getUuid());

        // LIVE UUID 처리: Event 생성 건너뛰고 Bin 상태 직접 업데이트
        if ("LIVE".equals(updateLiquidDTO.getUuid())) {
            bin.addWeight(liquidHistory.getAddedWeight());
            bin.updateLiquidWeight(liquidHistory.getWeight());
            binRepository.save(bin);
        } else {
            // Event 업데이트
            updateMainEvent(updateLiquidDTO.getUuid(), liquidHistory);
        }

        return liquid;
    }

    @Override
    public Liquid updateLiquidById(Long liquidId, LiquidRequestDTO.UpdateLiquidDTO updateLiquidDTO) {
        Liquid liquid = liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        // weight, addedWeight 업데이트
        updateLiquidWeight(liquid, updateLiquidDTO.getWeight(), updateLiquidDTO.getUuid());

        // LiquidHistory 기록 추가
        LiquidHistory liquidHistory = addLiquidHistory(liquid, updateLiquidDTO.getUuid());

        // LIVE UUID 처리: Event 생성 건너뛰고 Bin 상태 직접 업데이트
        if ("LIVE".equals(updateLiquidDTO.getUuid())) {
            Bin bin = liquid.getBin();
            bin.addWeight(liquidHistory.getAddedWeight());
            bin.updateLiquidWeight(liquidHistory.getWeight());
            binRepository.save(bin);
        } else {
            // Event 업데이트
            updateMainEvent(updateLiquidDTO.getUuid(), liquidHistory);
        }

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

        List<LiquidHistory> histories = findLiquidsByDate(binId, period, date);
        return LiquidConverter.toLiquidTrendDTO(binId, histories, period, mode);
    }

    @Override
    public Object readLiquidTrendById(Long liquidId, PeriodType period, LocalDate date, TrendMode mode) {
        Liquid liquid = liquidRepository.findById(liquidId).orElseThrow(() -> {
            throw new LiquidHandler(LiquidErrorCode._NOT_FOUND_LIQUID);
        });

        Long binId = liquid.getBin().getId();
        List<LiquidHistory> histories = findLiquidsByDate(binId, period, date);
        return LiquidConverter.toLiquidTrendDTO(binId, histories, period, mode);
    }

    public void updateLiquidWeight(Liquid liquid, double newWeight, String uuid) {
        double oldWeight = liquid.getWeight();
        double addedWeight = (newWeight > oldWeight) ? newWeight - oldWeight : 0;
        Boolean overloaded = (newWeight >= 4000);

        liquid.update(newWeight, addedWeight, overloaded, LocalDateTime.now(), uuid);
    }

    public LiquidHistory addLiquidHistory(Liquid liquid, String uuid) {
        LiquidHistory history = LiquidHistory.builder()
            .bin(liquid.getBin())
            .liquid(liquid)
            .weight(liquid.getWeight())
            .addedWeight(liquid.getAddedWeight())
            .overload(liquid.getOverloaded())
            .measuredAt(LocalDateTime.now())
            .uuid(uuid)
            .build();

        return liquidHistoryRepository.save(history);
    }

    // 이벤트 업데이트
    private void updateMainEvent(String uuid, LiquidHistory liquidHistory) {
        if (uuid == null || uuid.isEmpty()) {
            return;
        }
        eventService.registerSensorData(
            uuid,
            liquidHistory.getBin().getId(),
            EventService.SensorDataType.LIQUID,
            liquidHistory);
    }

    public List<LiquidHistory> findLiquidsByDate(Long binId, PeriodType period, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

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

        return liquidHistoryRepository.findByBinIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(binId, start, end);
    }

    @Override
    public Object getLiquidHistory(Long binId, String uuid, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 1);
        List<LiquidHistory> histories;

        if (uuid != null) {
            histories = liquidHistoryRepository.findByBinIdAndUuidOrderByMeasuredAtDesc(binId, uuid, pageable);
        } else {
            histories = liquidHistoryRepository.findByBinIdOrderByMeasuredAtDesc(binId, pageable);
        }

        if (limit == null) {
            return histories.isEmpty() ? null : convertToSensorHistoryDTO(histories.get(0));
        }

        return histories.stream()
            .map(this::convertToSensorHistoryDTO)
            .collect(Collectors.toList());
    }

    private SensorHistoryResponseDTO convertToSensorHistoryDTO(LiquidHistory history) {
        return SensorHistoryResponseDTO.builder()
            .timestamp(history.getMeasuredAt())
            .value(history.getWeight())
            .build();
    }
}
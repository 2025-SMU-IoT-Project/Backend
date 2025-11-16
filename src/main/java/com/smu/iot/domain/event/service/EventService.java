package com.smu.iot.domain.event.service;

import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.event.dto.response.EventDetailDTO;
import com.smu.iot.domain.event.dto.response.EventSummaryDTO;
import com.smu.iot.domain.event.entity.Event;
import com.smu.iot.domain.event.repository.EventRepository;
import com.smu.iot.domain.ir.entity.Ir;
import com.smu.iot.domain.laser.entity.CupShape;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final BinRepository binRepository;

    @Transactional
    public Event registerSensorData(String uuid, Long binId, SensorDataType type, Object sensorData) {
        Event event = getOrCreateEvent(uuid, binId);

        switch (type) {
            case IR -> {
                event.linkIrData((Ir) sensorData);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }

            case LASER -> {
                event.linkLaserData((CupShape) sensorData);
                if (((CupShape) sensorData).getIsValidCup() != null) {
                    event.setIsValidInput(((CupShape) sensorData).getIsValidCup());
                }
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }
            case CUP -> {
                event.linkCupData((Cup) sensorData);
                if (((Cup) sensorData).getIsLiquid() != null) {
                    event.setHasLiquid(((Cup) sensorData).getIsLiquid());
                }
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }
            case LIQUID -> {
                event.linkLiquidHistoryData((LiquidHistory) sensorData);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }
            case ULTRASONIC -> {
                event.linkUltrasonicData((Ultrasonic) sensorData);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event getOrCreateEvent(String uuid, Long binId) {
        // 먼저 조회 시도
        Event event = eventRepository.findByUuid(uuid).orElse(null);

        if (event != null) {
            return event;
        }

        // 없으면 생성 시도
        try {
            return createNewEvent(uuid, binId);
        } catch (DataIntegrityViolationException e) {
            // 동시에 생성되었을 경우 다시 조회
            log.warn("Concurrent event creation detected for UUID: {}, retrying...", uuid);
            return eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.INTERNAL_SERVER_ERROR_500));
        }
    }

    private Event createNewEvent(String uuid, Long binId) {
        Bin bin = binRepository.findById(binId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        Event event = Event.builder()
            .uuid(uuid)
            .bin(bin)
            .status(Event.EventStatus.INITIATED)
            .hasIrData(false)
            .hasLaserData(false)
            .hasCupData(false)
            .hasUltrasonicData(false)
            .hasLiquidData(false)
            .isValidInput(false)
            .hasLiquid(false)
            .cupAccepted(false)
            .startTime(LocalDateTime.now())
            .build();

        Event saved = eventRepository.save(event);
        log.info("New Event created - UUID: {}, BinId: {}", uuid, binId);
        return saved;
    }

    public EventDetailDTO getEventByUuid(String uuid) {
        log.info("Querying event by UUID: {}", uuid);

        Event event = eventRepository.findByUuid(uuid)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        return convertToDetailDTO(event);
    }

    public List<EventSummaryDTO> getRecentEvents(Long binId, int limit) {
        log.info("Querying recent events - binId: {}, limit: {}", binId, limit);

        List<Event> events = eventRepository.findByBin_IdOrderByCreatedAtDesc(binId);

        return events.stream()
            .limit(limit)
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }

    // Mock: 날짜 범위로 이벤트 조회
    public List<EventSummaryDTO> getEventsByDateRange(
        Long binId, LocalDateTime startDate, LocalDateTime endDate) {

        List<EventSummaryDTO> events = new ArrayList<>();

        // Mock: 날짜 범위 내 10개 이벤트 생성
        for (int i = 0; i < 10; i++) {
            events.add(EventSummaryDTO.builder()
                .uuid("550e8400-e29b-41d4-a716-" + String.format("%012d", i))
                .binId(binId)
                .timestamp(startDate.plusHours(i * 2))
                .isValidInput(true)
                .hasLiquid(i % 3 == 0)
                .cupType(i % 3 == 0 ? "LIQUID_CUP" : "EMPTY_CUP")
                .cupPattern("NORMAL_CUP")
                .build());
        }

        return events;
    }

    private EventDetailDTO convertToDetailDTO(Event event) {
        // IR 센서 데이터
        EventDetailDTO.IrSensorDTO irSensorDTO = null;
        if (event.getHasIrData() && event.getIrData() != null) {
            irSensorDTO = EventDetailDTO.IrSensorDTO.builder()
                .detected(true)
                .sensorId(event.getIrData().getSensorId())
                .beamBlocked(event.getIrData().getBeamBlocked())
                .timestamp(event.getIrTimestamp())
                .build();
        }

        // 레이저 센서 데이터
        EventDetailDTO.LaserSensorDTO laserSensorDTO = null;
        if (event.getHasLaserData() && event.getLaserData() != null) {
            // 샘플 데이터 변환
            List<EventDetailDTO.LaserSensorDTO.SampleData> samples = new ArrayList<>();
            if (event.getLaserData().getMeasurements() != null) {
                samples = event.getLaserData().getMeasurements().stream()
                    .map(m -> EventDetailDTO.LaserSensorDTO.SampleData.builder()
                        .timeMsec(m.getTimeMsec())
                        .distanceMm(m.getDistanceMm())
                        .build())
                    .collect(Collectors.toList());
            }

            laserSensorDTO = EventDetailDTO.LaserSensorDTO.builder()
                .detected(true)
                .isValidCup(event.getLaserData().getIsValidCup())
                .cupPattern(event.getLaserData().getPatternType().name())
                .minDistance(event.getLaserData().getMinDiameterMm())
                .maxDistance(event.getLaserData().getMaxDiameterMm())
                .avgDistance((event.getLaserData().getMinDiameterMm() +
                    event.getLaserData().getMaxDiameterMm()) / 2.0)
                .samples(samples)
                .timestamp(event.getLaserTimestamp())
                .build();
        }

        // 로드셀(컵통) 데이터
        EventDetailDTO.LoadCellDTO loadCellDTO = null;
        if (event.getHasCupData() && event.getCupData() != null) {
            loadCellDTO = EventDetailDTO.LoadCellDTO.builder()
                .detected(true)
                .weight(event.getCupData().getWeight())
                .isLiquid(event.getCupData().getIsLiquid())
                .cupType(event.getCupData().getCupType().name())
                .timestamp(event.getCupTimestamp())
                .build();
        }

        // 로드셀(물통) 데이터
        EventDetailDTO.LiquidDTO liquidDTO = null;
        if (event.getLiquidHistoryData() != null) {
            liquidDTO = EventDetailDTO.LiquidDTO.builder()
                .detected(true)
                .addedWeight(event.getLiquidHistoryData().getAddedWeight())
                .timestamp(event.getLiquidHistoryData().getMeasuredAt())
                .build();
        }

        // 초음파 센서 데이터
        EventDetailDTO.UltrasonicDTO ultrasonicDTO = null;
        if (event.getHasUltrasonicData() && event.getUltrasonicData() != null) {
            ultrasonicDTO = EventDetailDTO.UltrasonicDTO.builder()
                .detected(true)
                .distanceCm(event.getUltrasonicData().getDistanceCm())
                .fillRate(event.getUltrasonicData().getFillRate())
                .timestamp(event.getUltrasonicTimestamp())
                .build();
        }

        // 센서 데이터 통합
        EventDetailDTO.SensorDataDTO sensorDataDTO = EventDetailDTO.SensorDataDTO.builder()
            .irSensor(irSensorDTO)
            .laserSensor(laserSensorDTO)
            .loadCell(loadCellDTO)
            .liquid(liquidDTO)
            .ultrasonic(ultrasonicDTO)
            .build();

        // 이벤트 요약
        EventDetailDTO.EventSummaryDTO summaryDTO = EventDetailDTO.EventSummaryDTO.builder()
            .isValidInput(event.getIsValidInput())
            .hasLiquid(event.getHasLiquid())
            .cupAccepted(event.getCupAccepted())
            .processingTimeMs(event.getProcessingTimeMs())
            .build();

        return EventDetailDTO.builder()
            .uuid(event.getUuid())
            .binId(event.getBinId())
            .timestamp(event.getCreatedAt())
            .eventStatus(event.getStatus().name())
            .sensors(sensorDataDTO)
            .summary(summaryDTO)
            .build();
    }

    private EventSummaryDTO convertToSummaryDTO(Event event) {
        String cupType = "UNKNOWN";
        String cupPattern = "UNKNOWN";

        // 로드셀 데이터가 있으면 cupType 설정
        if (event.getHasCupData() && event.getCupData() != null) {
            cupType = event.getCupData().getCupType().name();
        }

        // 레이저 데이터가 있으면 cupPattern 설정
        if (event.getHasLaserData() && event.getLaserData() != null) {
            cupPattern = event.getLaserData().getPatternType().name();
        }

        return EventSummaryDTO.builder()
            .uuid(event.getUuid())
            .binId(event.getBinId())
            .timestamp(event.getCreatedAt())
            .isValidInput(event.getIsValidInput())
            .hasLiquid(event.getHasLiquid())
            .cupType(cupType)
            .cupPattern(cupPattern)
            .build();
    }

    public enum SensorDataType {
        IR, LASER, CUP, LIQUID, ULTRASONIC
    }
}
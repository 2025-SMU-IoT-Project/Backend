package com.smu.iot.domain.event.service;

import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.event.dto.response.EventDetailDTO;
import com.smu.iot.domain.event.dto.response.EventSummaryDTO;
import com.smu.iot.domain.event.entity.Event;
import com.smu.iot.domain.event.repository.EventRepository;
import com.smu.iot.domain.ir.entity.Ir;
import com.smu.iot.domain.ir.entity.code.CupType;
import com.smu.iot.domain.laser.entity.CupShape;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import com.smu.iot.global.apipayload.CursorResult;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                updateIsValidInput(event);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }
            }

            case LASER -> {
                event.linkLaserData((CupShape) sensorData);
                updateIsValidInput(event);
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

                // Bin 무게 업데이트
                Bin bin = event.getBin();
                bin.addWeight(((Cup) sensorData).getWeight());
                binRepository.save(bin);
            }
            case LIQUID -> {
                event.linkLiquidHistoryData((LiquidHistory) sensorData);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }

                // Bin 무게 업데이트
                Bin bin = event.getBin();
                bin.addWeight(((LiquidHistory) sensorData).getAddedWeight());
                bin.updateLiquidWeight(((LiquidHistory) sensorData).getWeight());
                binRepository.save(bin);
            }
            case ULTRASONIC -> {
                event.linkUltrasonicData((Ultrasonic) sensorData);
                if (event.isAllSensorDataReceived()) {
                    boolean cupAccepted = event.getIsValidInput() && !event.getHasLiquid();
                    event.setCupAccepted(cupAccepted);
                    event.completeEvent();
                }

                // Bin 상태 업데이트
                Bin bin = event.getBin();
                bin.updateFillLevel(((Ultrasonic) sensorData).getFillRate());
                binRepository.save(bin);
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

    public List<EventSummaryDTO> getAllEvents() {
        log.info("Querying all events");
        List<Event> events = eventRepository.findAll();
        return events.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventsByBinId(Long binId) {
        log.info("Querying events by binId: {}", binId);
        List<Event> events = eventRepository.findByBin_IdOrderByCreatedAtDesc(binId);
        return events.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }

    public List<EventSummaryDTO> getEventsByDateRange(
        Long binId,
        LocalDateTime startDate,
        LocalDateTime endDate) {

        log.info("Querying events by date range - binId: {}, start: {}, end: {}",
            binId, startDate, endDate);

        List<Event> events = eventRepository.findByBinIdAndDateRange(binId, startDate, endDate);

        return events.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());
    }

    public CursorResult<EventSummaryDTO> getEventsByCursor(Long binId, Long cursorId, int limit, Boolean onlyAbnormal) {
        log.info("Querying events by cursor - binId: {}, cursorId: {}, limit: {}, onlyAbnormal: {}", binId, cursorId, limit, onlyAbnormal);

        Pageable pageable = PageRequest.of(0, limit);
        List<Event> events;

        if (Boolean.TRUE.equals(onlyAbnormal)) {
            if (cursorId == null) {
                events = eventRepository.findAllByBin_IdAndCupAcceptedFalseOrderByIdDesc(binId, pageable);
            } else {
                events = eventRepository.findAllByBin_IdAndCupAcceptedFalseAndIdLessThanOrderByIdDesc(binId, cursorId,
                    pageable);
            }
        } else {
            if (cursorId == null) {
                events = eventRepository.findAllByBin_IdOrderByIdDesc(binId, pageable);
            } else {
                events = eventRepository.findAllByBin_IdAndIdLessThanOrderByIdDesc(binId, cursorId, pageable);
            }
        }

        return createCursorResult(events, limit);
    }

    public CursorResult<EventSummaryDTO> getAllEventsByCursor(Long cursorId, int limit, Boolean onlyAbnormal) {
        log.info("Querying all events by cursor - cursorId: {}, limit: {}, onlyAbnormal: {}", cursorId, limit, onlyAbnormal);

        Pageable pageable = PageRequest.of(0, limit);
        List<Event> events;

        if (Boolean.TRUE.equals(onlyAbnormal)) {
            if (cursorId == null) {
                events = eventRepository.findAllByCupAcceptedFalseOrderByIdDesc(pageable);
            } else {
                events = eventRepository.findAllByCupAcceptedFalseAndIdLessThanOrderByIdDesc(cursorId, pageable);
            }
        } else {
            if (cursorId == null) {
                events = eventRepository.findAllByOrderByIdDesc(pageable);
            } else {
                events = eventRepository.findAllByIdLessThanOrderByIdDesc(cursorId, pageable);
            }
        }

        return createCursorResult(events, limit);
    }

    private CursorResult<EventSummaryDTO> createCursorResult(List<Event> events, int limit) {
        List<EventSummaryDTO> eventDtos = events.stream()
            .map(this::convertToSummaryDTO)
            .collect(Collectors.toList());

        Long nextCursor = null;
        boolean hasNext = false;

        if (!events.isEmpty()) {
            nextCursor = events.get(events.size() - 1).getId();

            hasNext = events.size() == limit;
        }

        return new CursorResult<>(eventDtos, nextCursor, hasNext);
    }

    private EventDetailDTO convertToDetailDTO(Event event) {
        // IR 센서 데이터
        EventDetailDTO.IrSensorDTO irSensorDTO = null;
        if (event.getHasIrData() && event.getIrData() != null) {
            irSensorDTO = EventDetailDTO.IrSensorDTO.builder()
                .detected(true)
                .sensorId(event.getIrData().getSensorId())
                .beamBlocked(event.getIrData().getBeamBlocked())
                .isNormal(event.getIrData().getCupType() == CupType.PLASTIC)
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
                .isNormal(event.getLaserData().getIsValidCup())
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
                .isNormal(!event.getCupData().getIsLiquid())
                .timestamp(event.getCupTimestamp())
                .build();
        }

        // 로드셀(물통) 데이터
        EventDetailDTO.LiquidDTO liquidDTO = null;
        if (event.getLiquidHistoryData() != null) {
            liquidDTO = EventDetailDTO.LiquidDTO.builder()
                .detected(true)
                .addedWeight(event.getLiquidHistoryData().getAddedWeight())
                .isNormal(event.getLiquidHistoryData().getAddedWeight() <= 0) // 액체가 추가되지 않아야 정상
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
                .isNormal(event.getUltrasonicData().getFillRate() < 100.0)
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
            cupType = event.getCupData().getCupType().getDescription();
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
            .cupAccepted(event.getCupAccepted())
            .description(generateEventDescription(event))
            .build();
    }

    private String generateEventDescription(Event event) {
        if (event.getCupAccepted()) {
            return "정상적으로 처리된 컵입니다.";
        }

        StringBuilder reason = new StringBuilder("수거 거부됨: ");
        if (!event.getIsValidInput()) {
            reason.append("유효하지 않은 컵(종이컵 등) 또는 이물질 감지. ");
        }
        if (event.getHasLiquid()) {
            reason.append("컵 내부에 액체가 남아있음. ");
        }

        if (event.getRejectionReason() != null && !event.getRejectionReason().isEmpty()) {
            return "수거 거부됨: " + event.getRejectionReason();
        }

        return reason.toString().trim();
    }

    public enum SensorDataType {
        IR, LASER, CUP, LIQUID, ULTRASONIC
    }

    private void updateIsValidInput(Event event) {
        // IR 센서가 플라스틱 컵으로 판정했는지 확인
        boolean isPlasticCup = false;
        if (event.getIrData() != null && event.getIrData().getCupType() != null) {
            isPlasticCup = event.getIrData().getCupType() == CupType.PLASTIC;
        }

        // 레이저 센서가 유효한 컵으로 판정했는지 확인
        boolean isValidShape = false;
        if (event.getLaserData() != null && event.getLaserData().getIsValidCup() != null) {
            isValidShape = event.getLaserData().getIsValidCup();
        }

        // 두 조건이 모두 만족되어야 true
        if (event.getHasIrData() && event.getHasLaserData()) {
            // 두 센서 데이터가 모두 있을 때만 최종 판정
            event.setIsValidInput(isPlasticCup && isValidShape);

            if (!isPlasticCup) {
                event.setRejectionReason("종이컵은 투입할 수 없습니다.");
            } else if (!isValidShape) {
                event.setRejectionReason(event.getLaserData().getRejectionReason());
            } else {
                event.setRejectionReason(null);
            }
        } else {
            // 아직 데이터가 다 모이지 않았으면 임시로 false
            event.setIsValidInput(false);
        }
    }
}
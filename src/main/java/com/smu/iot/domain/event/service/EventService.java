package com.smu.iot.domain.event.service;

import com.smu.iot.domain.event.dto.response.EventDetailDTO;
import com.smu.iot.domain.event.dto.response.EventSummaryDTO;
import com.smu.iot.domain.event.entity.Event;
import com.smu.iot.domain.event.repository.EventRepository;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public EventDetailDTO getEventByUuid(String uuid) {
        log.info("Querying event by UUID: {}", uuid);

        Event event = eventRepository.findByUuid(uuid)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        return convertToDetailDTO(event);
    }

    // Mock: 최근 이벤트 목록 조회
    public List<EventSummaryDTO> getRecentEvents(Long binId, int limit) {
        List<EventSummaryDTO> events = new ArrayList<>();

        for (int i = 0; i < Math.min(limit, 20); i++) {
            boolean hasLiquid = i % 4 == 0; // 4개 중 1개는 액체 포함

            events.add(EventSummaryDTO.builder()
                .uuid("550e8400-e29b-41d4-a716-" + String.format("%012d", i))
                .binId(binId)
                .timestamp(LocalDateTime.now().minusMinutes(i * 10))
                .isValidInput(true)
                .hasLiquid(hasLiquid)
                .cupType(hasLiquid ? "LIQUID_CUP" : "EMPTY_CUP")
                .cupPattern("NORMAL_CUP")
                .build());
        }

        return events;
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

        // 로드셀 데이터
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
}
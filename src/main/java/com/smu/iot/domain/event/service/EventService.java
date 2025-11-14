package com.smu.iot.domain.event.service;

import com.smu.iot.domain.event.dto.response.EventDetailDTO;
import com.smu.iot.domain.event.dto.response.EventSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    // Mock: UUID로 통합 이벤트 조회
    public EventDetailDTO getEventByUuid(String uuid) {
        // Mock 샘플 데이터 생성
        List<EventDetailDTO.LaserSensorDTO.SampleData> samples = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            samples.add(EventDetailDTO.LaserSensorDTO.SampleData.builder()
                .timeMsec(i * 50)
                .distanceMm(145.5 - (i * 2.5))
                .build());
        }

        return EventDetailDTO.builder()
            .uuid(uuid)
            .binId(1L)
            .timestamp(LocalDateTime.now())
            .eventStatus("COMPLETED")
            .sensors(EventDetailDTO.SensorDataDTO.builder()
                .irSensor(EventDetailDTO.IrSensorDTO.builder()
                    .detected(true)
                    .sensorId("IR1")
                    .beamBlocked(true)
                    .timestamp(LocalDateTime.now().minusSeconds(15))
                    .build())
                .laserSensor(EventDetailDTO.LaserSensorDTO.builder()
                    .detected(true)
                    .isValidCup(true)
                    .cupPattern("NORMAL_CUP")
                    .minDistance(85.2)
                    .maxDistance(145.5)
                    .avgDistance(110.3)
                    .samples(samples)
                    .timestamp(LocalDateTime.now().minusSeconds(10))
                    .build())
                .loadCell(EventDetailDTO.LoadCellDTO.builder()
                    .detected(true)
                    .weight(5.5)
                    .isLiquid(false)
                    .cupType("EMPTY_CUP")
                    .timestamp(LocalDateTime.now().minusSeconds(5))
                    .build())
                .ultrasonic(EventDetailDTO.UltrasonicDTO.builder()
                    .detected(true)
                    .distanceCm(28.5)
                    .fillRate(43.0)
                    .timestamp(LocalDateTime.now())
                    .build())
                .build())
            .summary(EventDetailDTO.EventSummaryDTO.builder()
                .isValidInput(true)
                .hasLiquid(false)
                .cupAccepted(true)
                .processingTimeMs(15000L)
                .build())
            .build();
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
}
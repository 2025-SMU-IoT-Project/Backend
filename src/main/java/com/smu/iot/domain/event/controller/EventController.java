package com.smu.iot.domain.event.controller;

import com.smu.iot.domain.event.dto.response.EventDetailDTO;
import com.smu.iot.domain.event.dto.response.EventSummaryDTO;
import com.smu.iot.domain.event.service.EventService;
import com.smu.iot.global.apipayload.ApiResponse;
import com.smu.iot.global.apipayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "통합 이벤트", description = "UUID 기반 통합 센서 데이터 조회 API")
public class EventController {

    private final EventService eventService;

    @GetMapping("/{uuid}")
    @Operation(
        summary = "UUID로 모든 센서 데이터 통합 조회",
        description = "하나의 투입 이벤트에 대한 모든 센서 데이터를 조회"
    )
    public ApiResponse<EventDetailDTO> getEventByUuid(@PathVariable String uuid) {
        log.info("Querying event by UUID: {}", uuid);

        EventDetailDTO event = eventService.getEventByUuid(uuid);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, event);
    }

    @GetMapping("/recent")
    @Operation(
        summary = "최근 투입 이벤트 목록 조회",
        description = "특정 쓰레기통의 최근 투입 이벤트를 조회"
    )
    public ApiResponse<List<EventSummaryDTO>> getRecentEvents(
        @RequestParam(defaultValue = "1") Long binId,
        @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Querying recent events - binId: {}, limit: {}", binId, limit);

        List<EventSummaryDTO> events = eventService.getRecentEvents(binId, limit);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, events);
    }

    @GetMapping("/range")
    @Operation(
        summary = "특정 기간 투입 이벤트 조회",
        description = "특정 쓰레기통의 시작일과 종료일 사이의 투입 이벤트를 조회(DateTime)"
    )
    public ApiResponse<List<EventSummaryDTO>> getEventsByDateRange(
        @RequestParam(defaultValue = "1") Long binId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Querying events by date range - binId: {}, start: {}, end: {}", 
            binId, startDate, endDate);

        List<EventSummaryDTO> events = eventService.getEventsByDateRange(
            binId, startDate, endDate);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, events);
    }
}
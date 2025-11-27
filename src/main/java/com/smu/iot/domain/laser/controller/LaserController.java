package com.smu.iot.domain.laser.controller;

import com.smu.iot.domain.laser.code.LaserSuccessCode;
import com.smu.iot.domain.laser.dto.request.LaserPacketRequestDTO;
import com.smu.iot.domain.laser.dto.response.EventDetailResponseDTO;
import com.smu.iot.domain.laser.dto.response.InsertionEventResponseDTO;
import com.smu.iot.domain.laser.dto.response.InsertionStatsResponseDTO;
import com.smu.iot.domain.laser.service.LaserService;
import com.smu.iot.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sensor/laser")
@RequiredArgsConstructor
@Tag(name = "레이저 센서", description = "VL53L0X 레이저 거리 센서 API")
public class LaserController {

    private final LaserService laserService;

    @PostMapping("/insertion-event")
    @Operation(summary = "컵 투입 이벤트 처리 (패킷 전송)", description = "STM32에서 20개씩 나누어 전송되는 패킷을 수신하고, 모아서 처리")
    public ApiResponse<InsertionEventResponseDTO> processInsertionEvent(
        @RequestBody LaserPacketRequestDTO request) {

        log.info("Received laser packet - uuid: {}, idx: {}", request.getUuid(), request.getIdx());

        InsertionEventResponseDTO response = laserService.processPacket(request);

        if (response == null) {
            return ApiResponse.onSuccess(LaserSuccessCode.PACKET_RECEIVED, null);
        }

        // 유효한 컵 vs 거부된 컵에 따라 다른 SuccessCode 사용
        LaserSuccessCode successCode = response.getIsValidCup()
            ? LaserSuccessCode.VALID_CUP_DETECTED
            : LaserSuccessCode.INVALID_CUP_REJECTED;

        return ApiResponse.onSuccess(successCode, response);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "특정 이벤트 상세 조회", description = "응답에 측정값이 모두 포함됨")
    public ApiResponse<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId) {
        log.info("Querying event detail - eventId: {}", eventId);

        EventDetailResponseDTO response = laserService.getEventDetail(eventId);
        return ApiResponse.onSuccess(LaserSuccessCode.EVENT_DETAIL_RETRIEVED, response);
    }

    @GetMapping("/stats")
    @Operation(summary = "투입 이벤트 통계 조회", description = "전체/유효/거부 개수, 패턴별 통계, 최근 10개 이벤트 포함")
    public ApiResponse<InsertionStatsResponseDTO> getInsertionStats(
        @RequestParam(defaultValue = "1") Long binId) {

        log.info("Querying insertion stats - binId: {}", binId);

        InsertionStatsResponseDTO response = laserService.getInsertionStats(binId);
        return ApiResponse.onSuccess(LaserSuccessCode.STATS_RETRIEVED, response);
    }

    @GetMapping("/recent")
    @Operation(summary = "특정 쓰레기통의 최근 이벤트 조회")
    public ApiResponse<List<InsertionStatsResponseDTO.RecentEvent>> getRecentEvents(
        @RequestParam(defaultValue = "1") Long binId,
        @RequestParam(defaultValue = "10") int count) {

        log.info("Querying recent events - binId: {}, count: {}", binId, count);

        InsertionStatsResponseDTO stats = laserService.getInsertionStats(binId);
        return ApiResponse.onSuccess(LaserSuccessCode.STATS_RETRIEVED, stats.getRecentEvents());
    }

    @GetMapping("/event/uuid/{uuid}")
    public EventDetailResponseDTO getEventByUuid(
        @PathVariable String uuid) {
        return laserService.getEventDetailByUuid(uuid);
    }
}
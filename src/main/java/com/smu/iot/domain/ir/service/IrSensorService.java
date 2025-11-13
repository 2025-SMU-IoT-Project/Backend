package com.smu.iot.domain.ir.service;

import com.smu.iot.domain.ir.code.IrSensorErrorCode;
import com.smu.iot.domain.ir.dto.request.IrSensorEventDto;
import com.smu.iot.domain.ir.dto.response.SensorEventResponse;
import com.smu.iot.domain.ir.entity.CupInputRecord;
import com.smu.iot.domain.ir.entity.Ir;
import com.smu.iot.domain.ir.entity.code.CupType;
import com.smu.iot.domain.ir.entity.code.InputStatus;
import com.smu.iot.domain.ir.entity.code.SensorEventType;
import com.smu.iot.domain.ir.repository.CupInputRecordRepository;
import com.smu.iot.domain.ir.repository.IrSensorEventRepository;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IrSensorService {

    private final IrSensorEventRepository irSensorEventRepository;
    private final CupInputRecordRepository cupInputRecordRepository;

    @Transactional
    public SensorEventResponse processIrEvent(IrSensorEventDto dto) {

        log.info("Processing IR event - UUID: {}, SensorId: {}", dto.getUuid(), dto.getSensorId());

        // 센서 이벤트 저장
        Ir event = createAndSaveEvent(dto);

        // 센서 타입별 처리
        if ("IR1".equals(dto.getSensorId())) {
            return handleEntryDetection(event);
        } else if ("IR2".equals(dto.getSensorId())) {
            return handleCupTypeDetection(event);
        }

        throw new GeneralException(IrSensorErrorCode.UNKNOWN_SENSOR_ID);
    }

    private SensorEventResponse handleEntryDetection(Ir event) {

        if (Boolean.TRUE.equals(event.getBeamBlocked())) {
            log.info("Cup entry detected - UUID: {}, BinId: {}", event.getUuid(), event.getBinId());

            return SensorEventResponse.builder()
                .success(true)
                .message("컵 투입이 감지되었습니다.")
                .eventId(event.getId())
                .build();
        }

        return SensorEventResponse.builder()
            .success(true)
            .message("투입구가 비어있습니다.")
            .eventId(event.getId())
            .build();
    }

    @Transactional
    protected SensorEventResponse handleCupTypeDetection(Ir event) {

        CupType cupType;
        InputStatus status;
        String message;

        if (Boolean.TRUE.equals(event.getBeamBlocked())) {
            // 빔 차단 = 종이컵 (비정상)
            cupType = CupType.PAPER;
            status = InputStatus.REJECTED;
            message = "종이컵이 감지되었습니다. 플라스틱 컵만 투입 가능합니다.";
            log.warn("Paper cup detected - UUID: {}, BinId: {}", event.getUuid(), event.getBinId());

        } else {
            // 빔 통과 = 플라스틱 컵 (정상)
            cupType = CupType.PLASTIC;
            status = InputStatus.ACCEPTED;
            message = "플라스틱 컵 투입이 정상 처리되었습니다.";
            log.info("Plastic cup accepted - UUID: {}, BinId: {}", event.getUuid(), event.getBinId());
        }

        // 이벤트에 컵 타입 저장
        event.setCupType(cupType);
        irSensorEventRepository.save(event);

        // 투입 기록 생성
        CupInputRecord record = CupInputRecord.builder()
            .binId(event.getBinId())
            .cupType(cupType)
            .status(status)
            .rejectionReason(status == InputStatus.REJECTED ? "Paper cup not allowed" : null)
            .ir2Event(event)
            .build();

        cupInputRecordRepository.save(record);

        return SensorEventResponse.builder()
            .success(true)
            .detectedCupType(cupType)
            .status(status)
            .message(message)
            .eventId(event.getId())
            .build();
    }

    private Ir createAndSaveEvent(IrSensorEventDto dto) {

        SensorEventType eventType = determineEventType(dto.getSensorId());

        Ir event = Ir.builder()
            .uuid(dto.getUuid())
            .sensorId(dto.getSensorId())
            .binId(dto.getBinId())
            .beamBlocked(dto.getBeamBlocked())
            .eventType(eventType)
            .build();

        return irSensorEventRepository.save(event);
    }

    private SensorEventType determineEventType(String sensorId) {
        return "IR1".equals(sensorId)
            ? SensorEventType.ENTRY_DETECTED
            : SensorEventType.CUP_TYPE_DETECTED;
    }

    public List<Ir> getEventsByUuid(String uuid) {
        return irSensorEventRepository.findByUuid(uuid);
    }

    public List<Ir> getRecentEvents(String binId, int limit) {
        List<Ir> events = irSensorEventRepository.findByBinIdOrderByCreatedAtDesc(binId);
        return events.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

}
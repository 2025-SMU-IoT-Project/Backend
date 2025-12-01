package com.smu.iot.domain.laser.service;

import com.smu.iot.domain.event.service.EventService;
import com.smu.iot.domain.laser.code.LaserErrorCode;
import com.smu.iot.domain.laser.dto.request.InsertionEventRequestDTO;
import com.smu.iot.domain.laser.dto.response.EventDetailResponseDTO;
import com.smu.iot.domain.laser.dto.response.InsertionEventResponseDTO;
import com.smu.iot.domain.laser.dto.response.InsertionStatsResponseDTO;
import com.smu.iot.domain.laser.entity.CupShape;
import com.smu.iot.domain.laser.entity.CupShape.PatternType;
import com.smu.iot.domain.laser.entity.Laser;
import com.smu.iot.domain.laser.repository.InsertionEventRepository;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LaserService {

    private final InsertionEventRepository eventRepository;
    private final EventService eventService;
    private final com.smu.iot.domain.laser.repository.LaserRawDataRepository rawDataRepository;
    private final com.smu.iot.domain.bin.repository.BinRepository binRepository;
    private final com.smu.iot.domain.event.repository.EventRepository mainEventRepository;

    // 거리 기반 판단 상수 (mm)
    private static final int MOVING_AVERAGE_WINDOW = 5; // 이동 평균 윈도우 크기
    private static final double DETECT_THRESHOLD_MM = 450.0; // 물체 감지 임계값 (이보다 작으면 물체), 쓰리기통 너비 - 10 정도
    private static final double CUP_MIN_DIST_MM = 120.0; // 컵 유효 거리 최소값, 310 정도?
    private static final double CUP_MAX_DIST_MM = 480.0; // 컵 유효 거리 최대값, DETECT_THRESHOLD_MM와 동일하게
    private static final double MIN_VALID_DIAMETER = 20.0; // 유효 지름 최소값 (이보다 작으면 null 처리) 20
    private static final double CONSTANT_THRESHOLD = 20.0; // 일정 패턴 임계값 (mm)

    // 메인 처리 로직: STM32에서 받은 투입 이벤트 데이터 처리
    public InsertionEventResponseDTO processPacket(com.smu.iot.domain.laser.dto.request.LaserPacketRequestDTO request) {
        log.info("Received packet - UUID: {}, idx: {}", request.getUuid(), request.getIdx());

        // 1. 패킷 저장
        com.smu.iot.domain.laser.entity.LaserRawData rawData = com.smu.iot.domain.laser.entity.LaserRawData.builder()
            .uuid(request.getUuid())
            .idx(request.getIdx())
            .data(request.getData())
            .build();
        rawDataRepository.save(rawData);

        // 2. 전체 패킷 수신 여부 확인 (총 50개)
        long count = rawDataRepository.countByUuid(request.getUuid());
        if (count < 26) { // 50
            return null; // 아직 다 안 모임
        }

        // 3. 데이터 재조립
        List<com.smu.iot.domain.laser.entity.LaserRawData> allPackets = rawDataRepository
            .findAllByUuid(request.getUuid());
        allPackets.sort(java.util.Comparator.comparingInt(com.smu.iot.domain.laser.entity.LaserRawData::getIdx));

        List<InsertionEventRequestDTO.SampleData> samples = new ArrayList<>();
        int timeMsec = 0;
        for (com.smu.iot.domain.laser.entity.LaserRawData packet : allPackets) {
            for (Integer distance : packet.getData()) {
                samples.add(new InsertionEventRequestDTO.SampleData(timeMsec, distance.doubleValue()));
                timeMsec += 20; // 20ms 간격
            }
        }

        // 4. BinId 및 Width 조회
        Long binId = 1L; // Default
        Double binWidthMm = 490.0; // Default

        // UUID로 기존 이벤트 조회하여 BinId 확인
        java.util.Optional<com.smu.iot.domain.event.entity.Event> existingEvent = mainEventRepository
            .findByUuid(request.getUuid());
        if (existingEvent.isPresent()) {
            binId = existingEvent.get().getBin().getId();
            binWidthMm = existingEvent.get().getBin().getWidthMm();
        } else {
            // 이벤트가 없으면 BinId=1의 정보 가져오기
            com.smu.iot.domain.bin.entity.Bin bin = binRepository.findById(binId).orElse(null);
            if (bin != null) {
                binWidthMm = bin.getWidthMm();
            }
        }

        // 5. 통합 DTO 생성
        InsertionEventRequestDTO fullRequest = InsertionEventRequestDTO.builder()
            .uuid(request.getUuid())
            .binId(binId)
            .binWidthMm(binWidthMm)
            .samples(samples)
            .build();

        // 6. 처리 및 데이터 삭제
        InsertionEventResponseDTO response = processInsertionEvent(fullRequest);
        rawDataRepository.deleteAllByUuid(request.getUuid());

        return response;
    }

    // 메인 처리 로직: STM32에서 받은 투입 이벤트 데이터 처리
    public InsertionEventResponseDTO processInsertionEvent(InsertionEventRequestDTO request) {
        log.info("Processing insertion event - UUID: {}, binId: {}, samples: {}",
            request.getUuid(),
            request.getBinId(),
            request.getSamples() != null ? request.getSamples().size() : 0);

        try {
            // 1. 기본 검증
            validateInput(request);

            // 2. 이동 평균 필터 적용 및 거리 데이터 추출
            List<Double> smoothedDistances = applyMovingAverage(request.getSamples());

            // 3. 지름 계산
            List<Double> diameters = calculateDiameters(request.getBinWidthMm(), smoothedDistances);
            log.info("BinWidth: {}, Distances(0): {}, Diameter(0): {}",
                request.getBinWidthMm(),
                smoothedDistances.isEmpty() ? "empty" : smoothedDistances.get(0),
                diameters.isEmpty() ? "empty" : diameters.get(0));

            // 4. 패턴 분석
            PatternAnalysisResult analysisResult = analyzeCupPattern(smoothedDistances, diameters);
            log.info("Pattern analysis: type={}, valid={}, minDia={}, maxDia={}",
                analysisResult.getPatternType(), analysisResult.isValid(),
                analysisResult.getMinDiameter(), analysisResult.getMaxDiameter());

            // 4. 이벤트 엔티티 생성
            CupShape event = createInsertionEvent(request, analysisResult);

            // 5. 측정값 엔티티 생성 및 연결
            for (int i = 0; i < request.getSamples().size(); i++) {
                InsertionEventRequestDTO.SampleData sample = request.getSamples().get(i);
                Double diameter = diameters.get(i);

                // 40mm 미만인 지름은 유효하지 않은 값으로 간주하여 null 저장
                if (diameter != null && diameter < MIN_VALID_DIAMETER) {
                    diameter = null;
                }

                Laser measurement = Laser.builder()
                    .timeMsec(sample.getTimeMsec())
                    .distanceMm(sample.getDistanceMm())
                    .diameterMm(diameter)
                    .build();
                event.addMeasurement(measurement);
            }

            // 6. DB 저장
            CupShape savedEvent = eventRepository.save(event);

            // 7. Event 업데이트
            updateMainEvent(request.getUuid(), savedEvent);

            // 8. 응답 생성
            return buildResponse(savedEvent);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing insertion event", e);
            throw new GeneralException(LaserErrorCode.PROCESSING_FAILED);
        }
    }

    private void updateMainEvent(String uuid, CupShape laserData) {
        eventService.registerSensorData(
            uuid,
            laserData.getBinId(),
            EventService.SensorDataType.LASER,
            laserData);
    }

    // 입력 데이터 검증
    private void validateInput(InsertionEventRequestDTO request) {
        if (request.getSamples() == null || request.getSamples().isEmpty()) {
            throw new GeneralException(LaserErrorCode.INSUFFICIENT_SAMPLES);
        }

        if (request.getSamples().size() < MOVING_AVERAGE_WINDOW) {
            throw new GeneralException(LaserErrorCode.INSUFFICIENT_SAMPLES);
        }

        if (request.getBinWidthMm() == null || request.getBinWidthMm() <= 0) {
            throw new GeneralException(LaserErrorCode.INVALID_BIN_WIDTH);
        }
    }

    // 이동 평균 필터 적용
    private List<Double> applyMovingAverage(List<InsertionEventRequestDTO.SampleData> samples) {
        List<Double> smoothed = new ArrayList<>();
        double sum = 0;
        int count = 0;

        // 초기 윈도우 채우기 및 이동 평균 계산
        for (int i = 0; i < samples.size(); i++) {
            double val = samples.get(i).getDistanceMm();
            sum += val;
            count++;

            if (count > MOVING_AVERAGE_WINDOW) {
                sum -= samples.get(i - MOVING_AVERAGE_WINDOW).getDistanceMm();
                count--;
            }

            smoothed.add(sum / count);
        }
        return smoothed;
    }

    // 깊이 계산: 쓰레기통 너비 - 센서 거리
    private List<Double> calculateDiameters(double binWidthMm, List<Double> distances) {
        return distances.stream()
            .map(distance -> binWidthMm - distance)
            .collect(Collectors.toList());
    }

    // 패턴 분석: 거리 변화 패턴을 분석하여 유효성 판단
    private PatternAnalysisResult analyzeCupPattern(List<Double> distances, List<Double> diameters) {
        PatternAnalysisResult result = new PatternAnalysisResult();

        // 1. 물체 감지 여부 확인 (임계값 이하인 데이터가 있는지)
        // 배경보다 가까운 물체만 유효 데이터로 간주
        List<Double> validDistances = distances.stream()
            .filter(d -> d <= DETECT_THRESHOLD_MM)
            .toList();

        if (validDistances.isEmpty()) {
            result.setPatternType(PatternType.IRREGULAR);
            result.setValid(false);
            result.setRejectionReason("유효한 물체가 감지되지 않았습니다. (배경만 감지됨)");
            return result;
        }

        // 통계 계산 (전체 데이터 기준, 단 40mm 미만은 제외)
        List<Double> validDiametersForStats = diameters.stream()
            .filter(d -> d >= MIN_VALID_DIAMETER)
            .toList();

        Double minDiameter = null;
        Double maxDiameter = null;
        Double diameterChange = null;

        if (!validDiametersForStats.isEmpty()) {
            minDiameter = validDiametersForStats.stream().min(Double::compare).orElse(0.0);
            maxDiameter = validDiametersForStats.stream().max(Double::compare).orElse(0.0);
            diameterChange = maxDiameter - minDiameter;
        }

        result.setMinDiameter(minDiameter);
        result.setMaxDiameter(maxDiameter);
        result.setDiameterChange(diameterChange);

        // 1. 지름 변화량이 너무 작음 → 캔/병 (원통형)
        if (diameterChange != null && diameterChange < CONSTANT_THRESHOLD) {
            result.setPatternType(PatternType.CONSTANT);
            result.setValid(false);
            result.setRejectionReason("지름 변화가 없습니다. 캔 또는 병으로 추정됩니다.");
            return result;
        }

        // 2. 유효 범위 확인 (정상 컵 범위 내에 있는지)
        boolean isInValidRange = validDistances.stream()
            .anyMatch(d -> d >= CUP_MIN_DIST_MM && d <= CUP_MAX_DIST_MM);

        if (!isInValidRange) {
            result.setPatternType(PatternType.IRREGULAR);
            result.setValid(false);
            result.setRejectionReason("감지된 물체가 컵의 유효 거리 범위를 벗어났습니다.");
            return result;
        }

        // 3. 패턴 확인
        // 유효 데이터 중 시작(바닥)과 끝(입구) 부분의 평균 거리 비교
        int sampleSize = validDistances.size();
        int checkSize = Math.min(5, sampleSize / 3); // 앞뒤 1/3 지점 또는 5개 샘플 비교

        if (checkSize == 0) {
            // 데이터가 너무 적은 경우 단순 통과 (이미 유효 범위 체크는 통과했으므로)
            result.setPatternType(PatternType.NORMAL);
            result.setValid(true);
            return result;
        }

        double startAvg = 0;
        double endAvg = 0;
        for (int i = 0; i < checkSize; i++) {
            startAvg += validDistances.get(i);
            endAvg += validDistances.get(sampleSize - 1 - i);
        }
        startAvg /= checkSize;
        endAvg /= checkSize;

        // 바닥(Start)이 입구(End)보다 거리가 멀어야 함 (값이 커야 함)
        if (startAvg > endAvg) {
            result.setPatternType(PatternType.NORMAL);
            result.setValid(true);
        } else {
            result.setPatternType(PatternType.IRREGULAR);
            result.setValid(false);
            result.setRejectionReason("비정상 물체 탐지");
        }

        return result;
    }

    // 이벤트 엔티티 생성
    private CupShape createInsertionEvent(
        InsertionEventRequestDTO request,
        PatternAnalysisResult analysisResult) {

        return CupShape.builder()
            .uuid(request.getUuid())
            .binId(request.getBinId() != null ? request.getBinId() : 1L)
            .binWidthMm(request.getBinWidthMm())
            .isValidCup(analysisResult.isValid())
            .patternType(analysisResult.getPatternType())
            .minDiameterMm(analysisResult.getMinDiameter())
            .maxDiameterMm(analysisResult.getMaxDiameter())
            .diameterChangeMm(analysisResult.getDiameterChange())
            .rejectionReason(analysisResult.getRejectionReason())
            .sampleCount(request.getSamples().size())
            .build();
    }

    private InsertionEventResponseDTO buildResponse(
        CupShape event) {

        // 그래프용 지름 데이터 생성
        List<InsertionEventResponseDTO.DiameterData> diameterData = new ArrayList<>();
        for (int i = 0; i < event.getMeasurements().size(); i++) {
            Laser m = event.getMeasurements().get(i);
            diameterData.add(InsertionEventResponseDTO.DiameterData.builder()
                .timeMsec(m.getTimeMsec())
                .diameterMm(m.getDiameterMm())
                .build());
        }

        return InsertionEventResponseDTO.builder()
            .eventId(event.getId())
            .binId(event.getBinId())
            .isValidCup(event.getIsValidCup())
            .patternType(event.getPatternType().name())
            .patternDescription(event.getPatternType().getDescription())
            .minDiameterMm(event.getMinDiameterMm())
            .maxDiameterMm(event.getMaxDiameterMm())
            .diameterChangeMm(event.getDiameterChangeMm())
            .sampleCount(event.getSampleCount())
            .rejectionReason(event.getRejectionReason())
            .diameterData(diameterData)
            .build();
    }

    // 이벤트 상세 조회
    @Transactional(readOnly = true)
    public EventDetailResponseDTO getEventDetail(Long eventId) {
        CupShape event = eventRepository.findById(eventId)
            .orElseThrow(() -> new GeneralException(LaserErrorCode.EVENT_NOT_FOUND));

        return mapToEventDetailDTO(event);
    }

    @Transactional(readOnly = true)
    public EventDetailResponseDTO getEventDetailByUuid(String uuid) {
        CupShape event = eventRepository.findByUuid(uuid)
            .orElseThrow(() -> new GeneralException(LaserErrorCode.EVENT_NOT_FOUND));

        return mapToEventDetailDTO(event);
    }

    // 통계 조회
    @Transactional(readOnly = true)
    public InsertionStatsResponseDTO getInsertionStats(Long binId) {
        Long totalInsertions = eventRepository.countByBinId(binId);
        Long validCupCount = eventRepository.countByBinIdAndIsValidCupTrue(binId);
        Long rejectedCupCount = totalInsertions - validCupCount;

        double validPercentage = totalInsertions > 0
            ? (double) validCupCount / totalInsertions * 100
            : 0.0;

        // 패턴별 통계
        InsertionStatsResponseDTO.PatternStats patternStats = InsertionStatsResponseDTO.PatternStats.builder()
            .increasingCount(eventRepository.countByBinIdAndPatternType(binId, PatternType.NORMAL))
            .decreasingCount(eventRepository.countByBinIdAndPatternType(binId, PatternType.ABNORMAL))
            .constantCount(eventRepository.countByBinIdAndPatternType(binId, PatternType.CONSTANT))
            .irregularCount(eventRepository.countByBinIdAndPatternType(binId, PatternType.IRREGULAR))
            .build();

        // 최근 10개 이벤트
        List<CupShape> recentEvents = eventRepository.findTop10ByBinIdOrderByRegDateDesc(binId);
        List<InsertionStatsResponseDTO.RecentEvent> recentEventDTOs = recentEvents.stream()
            .map(e -> InsertionStatsResponseDTO.RecentEvent.builder()
                .eventId(e.getId())
                .regDate(e.getRegDate())
                .isValidCup(e.getIsValidCup())
                .patternType(e.getPatternType().name())
                .maxDiameterMm(e.getMaxDiameterMm())
                .rejectionReason(e.getRejectionReason())
                .build())
            .collect(Collectors.toList());

        return InsertionStatsResponseDTO.builder()
            .binId(binId)
            .totalInsertions(totalInsertions)
            .validCupCount(validCupCount)
            .rejectedCupCount(rejectedCupCount)
            .validCupPercentage(Math.round(validPercentage * 10) / 10.0)
            .patternStats(patternStats)
            .recentEvents(recentEventDTOs)
            .build();
    }

    // 패턴 분석 결과 내부 클래스
    @Data
    private static class PatternAnalysisResult {
        private PatternType patternType;
        private boolean valid;
        private String rejectionReason;
        private Double minDiameter;
        private Double maxDiameter;
        private Double diameterChange;
    }

    private EventDetailResponseDTO mapToEventDetailDTO(CupShape event) {
        List<EventDetailResponseDTO.MeasurementDetail> measurements = event.getMeasurements().stream()
            .map(m -> EventDetailResponseDTO.MeasurementDetail.builder()
                .measurementId(m.getId())
                .timeMsec(m.getTimeMsec())
                .distanceMm(m.getDistanceMm())
                .diameterMm(m.getDiameterMm())
                .regDate(m.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return EventDetailResponseDTO.builder()
            .eventId(event.getId())
            .regDate(event.getRegDate())
            .binId(event.getBinId())
            .isValidCup(event.getIsValidCup())
            .patternType(event.getPatternType().name())
            .minDiameterMm(event.getMinDiameterMm())
            .maxDiameterMm(event.getMaxDiameterMm())
            .diameterChangeMm(event.getDiameterChangeMm())
            .rejectionReason(event.getRejectionReason())
            .sampleCount(event.getSampleCount())
            .measurements(measurements)
            .build();
    }
}
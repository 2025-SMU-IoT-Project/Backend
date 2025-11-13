package com.smu.iot.domain.laser.service;

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

    // 유효성 검증 상수
    private static final int MIN_SAMPLE_COUNT = 10;  // 최소 샘플 수
    private static final double MIN_BOTTOM_DIAMETER = 40.0;  // 최소 하단 지름 (mm)
    private static final double MAX_BOTTOM_DIAMETER = 80.0;  // 최대 하단 지름 (mm)
    private static final double MIN_TOP_DIAMETER = 80.0;  // 최소 상단 지름 (mm)
    private static final double MAX_TOP_DIAMETER = 120.0;  // 최대 상단 지름 (mm)
    private static final double MIN_DIAMETER_CHANGE = 20.0;  // 최소 지름 변화량 (mm)
    private static final double CONSTANT_THRESHOLD = 20.0;  // 일정 패턴 임계값 (mm)

    // 메인 처리 로직: STM32에서 받은 투입 이벤트 데이터 처리
    public InsertionEventResponseDTO processInsertionEvent(InsertionEventRequestDTO request) {
        log.info("Processing insertion event - binId: {}, samples: {}",
            request.getBinId(),
            request.getSamples() != null ? request.getSamples().size() : 0);

        try {
            // 1. 기본 검증
            validateInput(request);

            // 2. 지름 계산
            List<Double> diameters = calculateDiameters(request);
            log.info("Calculated diameters: min={}, max={}",
                diameters.stream().min(Double::compare).orElse(0.0),
                diameters.stream().max(Double::compare).orElse(0.0));

            // 3. 패턴 분석
            PatternAnalysisResult analysisResult = analyzeCupPattern(diameters);
            log.info("Pattern analysis: type={}, valid={}",
                analysisResult.getPatternType(), analysisResult.isValid());

            // 4. 이벤트 엔티티 생성
            CupShape event = createInsertionEvent(request, analysisResult);

            // 5. 측정값 엔티티 생성 및 연결
            for (int i = 0; i < request.getSamples().size(); i++) {
                InsertionEventRequestDTO.SampleData sample = request.getSamples().get(i);
                Laser measurement = Laser.builder()
                    .timeMsec(sample.getTimeMsec())
                    .distanceMm(sample.getDistanceMm())
                    .diameterMm(diameters.get(i))
                    .build();
                event.addMeasurement(measurement);
            }

            // 6. DB 저장
            CupShape savedEvent = eventRepository.save(event);
            log.info("Event saved: id={}, valid={}", savedEvent.getId(), savedEvent.getIsValidCup());

            // 7. 응답 생성
            return buildResponse(savedEvent);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing insertion event", e);
            throw new GeneralException(LaserErrorCode.PROCESSING_FAILED);
        }
    }

    // 입력 데이터 검증
    private void validateInput(InsertionEventRequestDTO request) {
        if (request.getSamples() == null || request.getSamples().isEmpty()) {
            throw new GeneralException(LaserErrorCode.INSUFFICIENT_SAMPLES);
        }

        if (request.getSamples().size() < MIN_SAMPLE_COUNT) {
            throw new GeneralException(LaserErrorCode.INSUFFICIENT_SAMPLES);
        }

        if (request.getBinWidthMm() == null || request.getBinWidthMm() <= 0) {
            throw new GeneralException(LaserErrorCode.INVALID_BIN_WIDTH);
        }
    }

    // 지름 계산: 쓰레기통 너비 - (2 × 센서 거리)
    private List<Double> calculateDiameters(InsertionEventRequestDTO request) {
        return request.getSamples().stream()
            .map(sample -> Laser.calculateDiameter(
                request.getBinWidthMm(),
                sample.getDistanceMm()))
            .collect(Collectors.toList());
    }

    // 패턴 분석: 지름 변화 패턴을 분석하여 유효성 판단
    private PatternAnalysisResult analyzeCupPattern(List<Double> diameters) {
        PatternAnalysisResult result = new PatternAnalysisResult();

        double minDiameter = diameters.stream().min(Double::compare).orElse(0.0);
        double maxDiameter = diameters.stream().max(Double::compare).orElse(0.0);
        double diameterChange = maxDiameter - minDiameter;

        result.setMinDiameter(minDiameter);
        result.setMaxDiameter(maxDiameter);
        result.setDiameterChange(diameterChange);

        // 1. 지름 변화량이 너무 작음 → 캔/병 (원통형)
        if (diameterChange < CONSTANT_THRESHOLD) {
            result.setPatternType(PatternType.CONSTANT);
            result.setValid(false);
            result.setRejectionReason("지름 변화가 없습니다. 캔 또는 병으로 추정됩니다.");
            return result;
        }

        // 2. 정상 패턴 체크 (60→100mm)
        if (isNormalPattern(diameters)) {
            result.setPatternType(PatternType.NORMAL);

            // 지름 범위 검증
            if (!validateDiameterRange(minDiameter, maxDiameter)) {
                result.setValid(false);
                result.setRejectionReason(
                    String.format("지름 범위가 비정상입니다. (하단: %.1fmm, 상단: %.1fmm)",
                        minDiameter, maxDiameter));
                return result;
            }

            // 지름 변화량 검증
            if (diameterChange < MIN_DIAMETER_CHANGE) {
                result.setValid(false);
                result.setRejectionReason(
                    String.format("지름 변화량이 부족합니다. (변화량: %.1fmm, 최소: %.1fmm)",
                        diameterChange, MIN_DIAMETER_CHANGE));
                return result;
            }

            result.setValid(true);
            result.setRejectionReason(null);
            return result;
        }

        // 3. 비정상 패턴 체크 (100→60mm)
        if (isDecreasingPattern(diameters)) {
            result.setPatternType(PatternType.ABNORMAL);
            result.setValid(false);
            result.setRejectionReason("컵이 뒤집혀서 투입되었습니다.");
            return result;
        }

        // 4. 불규칙 패턴
        result.setPatternType(PatternType.IRREGULAR);
        result.setValid(false);
        result.setRejectionReason("불규칙한 형태가 감지되었습니다.");
        return result;
    }

    // 정상 패턴 체크
    private boolean isNormalPattern(List<Double> diameters) {
        int increasingCount = 0;
        int totalPairs = diameters.size() - 1;

        for (int i = 0; i < totalPairs; i++) {
            if (diameters.get(i + 1) >= diameters.get(i)) {
                increasingCount++;
            }
        }

        // 80% 이상이 증가하면 정상으로 판단
        double increasingRatio = (double) increasingCount / totalPairs;
        return increasingRatio >= 0.8;
    }

    // 감소 패턴 체크
    private boolean isDecreasingPattern(List<Double> diameters) {
        int decreasingCount = 0;
        int totalPairs = diameters.size() - 1;

        for (int i = 0; i < totalPairs; i++) {
            if (diameters.get(i + 1) <= diameters.get(i)) {
                decreasingCount++;
            }
        }

        // 80% 이상이 감소하면 정상으로 판단
        double decreasingRatio = (double) decreasingCount / totalPairs;
        return decreasingRatio >= 0.8;
    }

    // 지름 범위 검증
    private boolean validateDiameterRange(double minDiameter, double maxDiameter) {
        boolean validBottom = minDiameter >= MIN_BOTTOM_DIAMETER && minDiameter <= MAX_BOTTOM_DIAMETER;
        boolean validTop = maxDiameter >= MIN_TOP_DIAMETER && maxDiameter <= MAX_TOP_DIAMETER;
        return validBottom && validTop;
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

        // 헬퍼 메서드 사용하도록 변경
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
        private double minDiameter;
        private double maxDiameter;
        private double diameterChange;
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
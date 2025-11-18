package com.smu.iot.domain.bin.service;

import com.smu.iot.domain.bin.dto.request.BinCreateRequestDTO;
import com.smu.iot.domain.bin.dto.request.BinUpdateRequestDTO;
import com.smu.iot.domain.bin.dto.response.*;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.domain.event.entity.Event;
import com.smu.iot.domain.event.repository.EventRepository;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.liquid.repository.LiquidHistoryRepository;
import com.smu.iot.domain.liquid.repository.LiquidRepository;
import com.smu.iot.domain.loadcell.repository.BinWeightRepository;
import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import com.smu.iot.domain.ultrasonic.repository.UltrasonicRepository;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinService {

    private final BinRepository binRepository;
    private final EventRepository eventRepository;
    private final UltrasonicRepository ultrasonicRepository;
    private final BinWeightRepository binWeightRepository;
    private final LiquidRepository liquidRepository;
    private final LiquidHistoryRepository liquidHistoryRepository;

    public BinInfoDTO getBinInfo(Long binId) {

        Bin bin = binRepository.findById(binId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        BinInfoDTO.LocationDTO locationDTO = BinInfoDTO.LocationDTO.builder()
            .building(bin.getBuilding())
            .floor(bin.getFloor())
            .room(bin.getRoom())
            .latitude(bin.getLatitude())
            .longitude(bin.getLongitude())
            .address(bin.getAddress())
            .build();

        BinInfoDTO.SpecificationDTO specificationDTO = BinInfoDTO.SpecificationDTO.builder()
            .capacity(bin.getCapacity())
            .heightCm(bin.getHeightCm())
            .widthMm(bin.getWidthMm())
            .maxWeight(bin.getMaxWeight())
            .build();

        BinInfoDTO.StatusDTO statusDTO = BinInfoDTO.StatusDTO.builder()
            .isActive(bin.getIsActive())
            .isOnline(bin.getIsOnline())
            .lastHeartbeat(bin.getUpdatedAt())
            .installDate(bin.getCreatedAt().toLocalDate())
            .fillStatus(bin.getStatus())
            .build();

        return BinInfoDTO.builder()
            .binId(bin.getId())
            .binName(bin.getName())
            .binCode(bin.getBinCode())
            .location(locationDTO)
            .specifications(specificationDTO)
            .status(statusDTO)
            .build();
    }

    // Mock: 쓰레기통 현재 상태 조회
    public BinStatusDTO getBinStatus(Long binId) {
        Bin bin = binRepository.findById(binId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));


        return BinStatusDTO.builder()
            .binId(binId)
            .binName("공학관 1층 쓰레기통")
            .currentStatus(BinStatusDTO.CurrentStatusDTO.builder()
                .fillRate(65.5)
                .fillLevel("MEDIUM")
                .distanceCm(17.25)
                .totalWeight(2850.5)
                .needsCollection(false)
                .lastUpdated(LocalDateTime.now())
                .build())
            .todayStats(BinStatusDTO.TodayStatsDTO.builder()
                .totalInputs(87)
                .validCups(75)
                .invalidCups(12)
                .emptyCups(60)
                .liquidCups(15)
                .liquidRate(17.24)
                .build())
            .sensorStatus(BinStatusDTO.SensorStatusDTO.builder()
                .irSensor(BinStatusDTO.SensorStatusDTO.SensorInfo.builder()
                    .status("ONLINE")
                    .lastActive(LocalDateTime.now())
                    .build())
                .laserSensor(BinStatusDTO.SensorStatusDTO.SensorInfo.builder()
                    .status("ONLINE")
                    .lastActive(LocalDateTime.now())
                    .build())
                .loadCell(BinStatusDTO.SensorStatusDTO.SensorInfo.builder()
                    .status("ONLINE")
                    .lastActive(LocalDateTime.now())
                    .build())
                .ultrasonic(BinStatusDTO.SensorStatusDTO.SensorInfo.builder()
                    .status("ONLINE")
                    .lastActive(LocalDateTime.now())
                    .build())
                .build())
            .build();
    }

    // Mock: 전체 쓰레기통 목록 조회
    public List<BinListDTO> getAllBins(Boolean includeStatus) {
        List<BinListDTO> bins = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            bins.add(BinListDTO.builder()
                .binId((long) i)
                .binName("공학관 " + i + "층 쓰레기통")
                .location(BinListDTO.LocationInfoDTO.builder()
                    .building("공학관")
                    .floor(i)
                    .latitude(37.5665 + (i * 0.0001))
                    .longitude(126.9780 + (i * 0.0001))
                    .build())
                .status(includeStatus ? BinListDTO.StatusInfoDTO.builder()
                    .isOnline(true)
                    .fillRate(50.0 + (i * 5))
                    .needsCollection(i > 3)
                    .build() : null)
                .build());
        }

        return bins;
    }

    // Mock: 수거가 필요한 쓰레기통 목록
    public BinCollectionDTO getBinsNeedingCollection(Double threshold) {
        List<BinCollectionDTO.CollectionBinDTO> bins = new ArrayList<>();

        bins.add(BinCollectionDTO.CollectionBinDTO.builder()
            .binId(2L)
            .binName("공학관 2층 쓰레기통")
            .fillRate(85.2)
            .totalWeight(4200.5)
            .priority("HIGH")
            .location(BinCollectionDTO.CollectionBinDTO.LocationDTO.builder()
                .building("공학관")
                .floor(2)
                .latitude(37.5666)
                .longitude(126.9781)
                .build())
            .build());

        bins.add(BinCollectionDTO.CollectionBinDTO.builder()
            .binId(5L)
            .binName("공학관 1층 쓰레기통")
            .fillRate(82.1)
            .totalWeight(3950.0)
            .priority("HIGH")
            .location(BinCollectionDTO.CollectionBinDTO.LocationDTO.builder()
                .building("공학관")
                .floor(1)
                .latitude(37.5668)
                .longitude(126.9785)
                .build())
            .build());

        return BinCollectionDTO.builder()
            .totalBins(10)
            .needsCollectionCount(2)
            .bins(bins)
            .build();
    }

    // Mock: 쓰레기통 생성
    public BinInfoDTO createBin(BinCreateRequestDTO request) {
        return BinInfoDTO.builder()
            .binId(99L)
            .binName(request.getBinName())
            .binCode(request.getBinCode())
            .location(BinInfoDTO.LocationDTO.builder()
                .building(request.getLocation().getBuilding())
                .floor(request.getLocation().getFloor())
                .room(request.getLocation().getRoom())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .build())
            .specifications(BinInfoDTO.SpecificationDTO.builder()
                .capacity(request.getSpecifications().getCapacity())
                .heightCm(request.getSpecifications().getHeightCm())
                .widthMm(request.getSpecifications().getWidthMm())
                .maxWeight(request.getSpecifications().getMaxWeight())
                .build())
            .status(BinInfoDTO.StatusDTO.builder()
                .isActive(true)
                .isOnline(false)
                .lastHeartbeat(null)
                .installDate(LocalDate.now())
                .build())
            .build();
    }

    // Mock: 쓰레기통 수정
    public BinInfoDTO updateBin(Long binId, BinUpdateRequestDTO request) {
        return BinInfoDTO.builder()
            .binId(binId)
            .binName(request.getBinName())
            .binCode("BIN-001")
            .location(BinInfoDTO.LocationDTO.builder()
                .building(request.getLocation().getBuilding())
                .floor(request.getLocation().getFloor())
                .room(request.getLocation().getRoom())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .build())
            .specifications(BinInfoDTO.SpecificationDTO.builder()
                .capacity(request.getSpecifications().getCapacity())
                .heightCm(request.getSpecifications().getHeightCm())
                .widthMm(request.getSpecifications().getWidthMm())
                .maxWeight(request.getSpecifications().getMaxWeight())
                .build())
            .status(BinInfoDTO.StatusDTO.builder()
                .isActive(true)
                .isOnline(true)
                .lastHeartbeat(LocalDateTime.now())
                .installDate(LocalDate.of(2025, 10, 1))
                .build())
            .build();
    }

    public BinGlobalStatsDTO getBinGlobalStats(String period) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = calculateStartDate(period);

        // 해당 기간의 전체 이벤트 조회
        List<Event> events = eventRepository.findAllByDateRange(start, end);

        long totalCups = events.size();

        // Event 엔티티의 필드를 사용하여 집계
        long liquidCups = events.stream()
            .filter(Event::getHasLiquid)
            .count();

        // isValidInput이 false인 경우를 비정상 투입으로 간주
        long abnormalCount = events.stream()
            .filter(e -> !e.getIsValidInput())
            .count();

        double liquidRate = totalCups > 0
            ? (double) liquidCups / totalCups * 100.0
            : 0.0;

        // 전체 쓰레기통의 최근 채움률 평균 계산
        List<Bin> allBins = binRepository.findAll();
        List<Double> recentFillRates = new ArrayList<>();

        for (Bin bin : allBins) {
            // 해당 기간 내의 최신 데이터를 조회
            List<Ultrasonic> recentData = ultrasonicRepository
                .findTop5ByBinIdAndCreatedAtBetweenOrderByCreatedAtDesc(bin.getId(), start, end);

            // 만약 기간 내 데이터가 없다면, 기간 상관없이 가장 최근 데이터를 조회
            if (recentData.isEmpty()) {
                recentData = ultrasonicRepository.findTop5ByBinIdOrderByCreatedAtDesc(bin.getId());
            }

            // 데이터 수집
            for (Ultrasonic u : recentData) {
                recentFillRates.add(u.getFillRate());
            }
        }

        // 수집된 데이터들의 평균 계산
        double averageFillRate = recentFillRates.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        return BinGlobalStatsDTO.builder()
            .totalCups(totalCups)
            .liquidRate(Math.round(liquidRate * 10) / 10.0)
            .abnormalCount(abnormalCount)
            .averageFillRate(Math.round(averageFillRate * 10) / 10.0)
            .period(period.toUpperCase())
            .build();
    }

    private LocalDateTime calculateStartDate(String period) {
        LocalDate today = LocalDate.now();

        return switch (period.toUpperCase()) {
            case "WEEKLY" -> today.minusWeeks(1).atStartOfDay();
            case "MONTHLY" -> today.minusMonths(1).atStartOfDay();
            case "DAILY" -> today.atStartOfDay();
            default -> today.atStartOfDay();
        };
    }

    public BinDetailDTO getBinDetail(Long binId) {
        // Bin 존재 여부 확인
        Bin bin = binRepository.findById(binId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        // 투입 통계
        long totalCups = eventRepository.countByBin_Id(binId);
        long abnormalCount = eventRepository.countByBin_IdAndIsValidInputFalse(binId);

        // 채움률 - 최근 5개 평균
        List<Ultrasonic> recentUltrasonics = ultrasonicRepository.findTop5ByBinIdOrderByCreatedAtDesc(binId);
        double fillRate = recentUltrasonics.stream()
            .mapToDouble(Ultrasonic::getFillRate)
            .average()
            .orElse(0.0);

        // 컵통 무게
        double cupWeightKg = binWeightRepository.findByBinId(binId)
            .map(bw -> bw.getCurrentWeight() / 1000.0) // 단위 kg
            .orElse(0.0);

        // 물통 무게
        double liquidWeightKg = liquidRepository.findByBin(bin)
            .map(l -> l.getWeight() / 1000.0) // 단위 kg
            .orElse(0.0);

        // 액체 채움률 계산
        double liquidRate = (liquidWeightKg / 5.0) * 100.0; // 일단 물통 최대 무게가 5kg라고 가정

        return BinDetailDTO.builder()
            .binId(binId)
            .totalCups(totalCups)
            .abnormalCount(abnormalCount)
            .fillRate(Math.round(fillRate * 10) / 10.0)
            .cupWeight(Math.round(cupWeightKg * 10) / 10.0)
            .liquidWeight(Math.round(liquidWeightKg * 10) / 10.0)
            .liquidRate(Math.round(liquidRate * 10) / 10.0)
            .build();
    }

    public BinTrendResponseDTO getCupTrend(Long binId, String period, String dateStr) {
        List<BinTrendResponseDTO.TrendPoint> points = new ArrayList<>();

        if ("MONTHLY".equalsIgnoreCase(period)) {
            if (dateStr == null) dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            YearMonth yearMonth = YearMonth.parse(dateStr);

            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            // 해당 월 데이터 조회
            List<Event> events = eventRepository.findByBin_IdAndCreatedAtBetween(binId, start, end);
            DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM-dd");

            // 1일 ~ 말일
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                int currentDay = day;
                long count = events.stream()
                    .filter(e -> e.getCreatedAt().getDayOfMonth() == currentDay)
                    .count();

                points.add(new BinTrendResponseDTO.TrendPoint(
                    yearMonth.atDay(day).format(labelFormatter), (double) count));
            }

        } else {
            LocalDate date = (dateStr == null) ? LocalDate.now() : LocalDate.parse(dateStr);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            // 해당 일 데이터 조회
            List<Event> events = eventRepository.findByBin_IdAndCreatedAtBetween(binId, start, end);

            // 0시 ~ 23시
            for (int hour = 0; hour < 24; hour++) {
                int currentHour = hour;
                long count = events.stream()
                    .filter(e -> e.getCreatedAt().getHour() == currentHour)
                    .count();

                points.add(new BinTrendResponseDTO.TrendPoint(
                    String.format("%02d:00", hour), (double) count));
            }
            dateStr = date.toString();
        }

        return BinTrendResponseDTO.builder()
            .binId(binId)
            .type("CUP")
            .period(period.toUpperCase())
            .baseDate(dateStr)
            .trends(points)
            .build();
    }

    public BinTrendResponseDTO getLiquidTrend(Long binId, String period, String dateStr) {
        List<BinTrendResponseDTO.TrendPoint> points = new ArrayList<>();

        if ("MONTHLY".equalsIgnoreCase(period)) {
            if (dateStr == null) dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            YearMonth yearMonth = YearMonth.parse(dateStr);

            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<LiquidHistory> liquids = liquidHistoryRepository
                .findByBinIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(binId, start, end);
            DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd");

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                int currentDay = day;
                double weightSum = liquids.stream()
                    .filter(l -> l.getMeasuredAt().getDayOfMonth() == currentDay)
                    .mapToDouble(LiquidHistory::getAddedWeight)
                    .sum();

                points.add(new BinTrendResponseDTO.TrendPoint(
                    yearMonth.atDay(day).format(labelFormatter), weightSum));
            }

        } else {
            LocalDate date = (dateStr == null) ? LocalDate.now() : LocalDate.parse(dateStr);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            List<LiquidHistory> liquids = liquidHistoryRepository
                .findByBinIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(binId, start, end);

            for (int hour = 0; hour < 24; hour++) {
                int currentHour = hour;
                double weightSum = liquids.stream()
                    .filter(l -> l.getMeasuredAt().getHour() == currentHour)
                    .mapToDouble(LiquidHistory::getAddedWeight)
                    .sum();

                points.add(new BinTrendResponseDTO.TrendPoint(
                    String.format("%02d", hour), weightSum));
            }
            dateStr = date.toString();
        }

        return BinTrendResponseDTO.builder()
            .binId(binId)
            .type("LIQUID")
            .period(period.toUpperCase())
            .baseDate(dateStr)
            .trends(points)
            .build();
    }
}
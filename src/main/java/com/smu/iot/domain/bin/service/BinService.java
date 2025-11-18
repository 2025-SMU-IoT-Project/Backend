package com.smu.iot.domain.bin.service;

import com.smu.iot.domain.bin.dto.request.BinCreateRequestDTO;
import com.smu.iot.domain.bin.dto.request.BinUpdateRequestDTO;
import com.smu.iot.domain.bin.dto.response.BinCollectionDTO;
import com.smu.iot.domain.bin.dto.response.BinDetailDTO;
import com.smu.iot.domain.bin.dto.response.BinListDTO;
import com.smu.iot.domain.bin.dto.response.BinStatusDTO;
import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.bin.repository.BinRepository;
import com.smu.iot.global.apipayload.code.GeneralErrorCode;
import com.smu.iot.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinService {

    private final BinRepository binRepository;

    public BinDetailDTO getBinDetail(Long binId) {

        Bin bin = binRepository.findById(binId)
            .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_404));

        BinDetailDTO.LocationDTO locationDTO = BinDetailDTO.LocationDTO.builder()
            .building(bin.getBuilding())
            .floor(bin.getFloor())
            .room(bin.getRoom())
            .latitude(bin.getLatitude())
            .longitude(bin.getLongitude())
            .address(bin.getAddress())
            .build();

        BinDetailDTO.SpecificationDTO specificationDTO = BinDetailDTO.SpecificationDTO.builder()
            .capacity(bin.getCapacity())
            .heightCm(bin.getHeightCm())
            .widthMm(bin.getWidthMm())
            .maxWeight(bin.getMaxWeight())
            .build();

        BinDetailDTO.StatusDTO statusDTO = BinDetailDTO.StatusDTO.builder()
            .isActive(bin.getIsActive())
            .isOnline(bin.getIsOnline())
            .lastHeartbeat(bin.getUpdatedAt())
            .installDate(bin.getCreatedAt().toLocalDate())
            .fillStatus(bin.getStatus())
            .build();

        return BinDetailDTO.builder()
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
    public BinDetailDTO createBin(BinCreateRequestDTO request) {
        return BinDetailDTO.builder()
            .binId(99L)
            .binName(request.getBinName())
            .binCode(request.getBinCode())
            .location(BinDetailDTO.LocationDTO.builder()
                .building(request.getLocation().getBuilding())
                .floor(request.getLocation().getFloor())
                .room(request.getLocation().getRoom())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .build())
            .specifications(BinDetailDTO.SpecificationDTO.builder()
                .capacity(request.getSpecifications().getCapacity())
                .heightCm(request.getSpecifications().getHeightCm())
                .widthMm(request.getSpecifications().getWidthMm())
                .maxWeight(request.getSpecifications().getMaxWeight())
                .build())
            .status(BinDetailDTO.StatusDTO.builder()
                .isActive(true)
                .isOnline(false)
                .lastHeartbeat(null)
                .installDate(LocalDate.now())
                .build())
            .build();
    }

    // Mock: 쓰레기통 수정
    public BinDetailDTO updateBin(Long binId, BinUpdateRequestDTO request) {
        return BinDetailDTO.builder()
            .binId(binId)
            .binName(request.getBinName())
            .binCode("BIN-001")
            .location(BinDetailDTO.LocationDTO.builder()
                .building(request.getLocation().getBuilding())
                .floor(request.getLocation().getFloor())
                .room(request.getLocation().getRoom())
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .build())
            .specifications(BinDetailDTO.SpecificationDTO.builder()
                .capacity(request.getSpecifications().getCapacity())
                .heightCm(request.getSpecifications().getHeightCm())
                .widthMm(request.getSpecifications().getWidthMm())
                .maxWeight(request.getSpecifications().getMaxWeight())
                .build())
            .status(BinDetailDTO.StatusDTO.builder()
                .isActive(true)
                .isOnline(true)
                .lastHeartbeat(LocalDateTime.now())
                .installDate(LocalDate.of(2025, 10, 1))
                .build())
            .build();
    }
}
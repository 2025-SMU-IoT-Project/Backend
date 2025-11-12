package com.smu.iot.domain.laser.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailResponseDTO {

    private Long eventId;
    private LocalDateTime regDate;
    private Long binId;
    private Boolean isValidCup;
    private String patternType;
    private Double minDiameterMm;
    private Double maxDiameterMm;
    private Double diameterChangeMm;
    private String rejectionReason;
    private Integer sampleCount;
    private List<MeasurementDetail> measurements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeasurementDetail {
        private Long measurementId;
        private Integer timeMsec;
        private Double distanceMm;
        private Double diameterMm;
        private LocalDateTime regDate;
    }
}
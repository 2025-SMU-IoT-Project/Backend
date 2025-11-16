package com.smu.iot.domain.event.dto.response;

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
public class EventDetailDTO {
    private String uuid;
    private Long binId;
    private LocalDateTime timestamp;
    private String eventStatus;
    private SensorDataDTO sensors;
    private EventSummaryDTO summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorDataDTO {
        private IrSensorDTO irSensor;
        private LaserSensorDTO laserSensor;
        private LoadCellDTO loadCell;
        private LiquidDTO liquid;
        private UltrasonicDTO ultrasonic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IrSensorDTO {
        private Boolean detected;
        private String sensorId;
        private Boolean beamBlocked;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LaserSensorDTO {
        private Boolean detected;
        private Boolean isValidCup;
        private String cupPattern;
        private Double minDistance;
        private Double maxDistance;
        private Double avgDistance;
        private List<SampleData> samples;
        private LocalDateTime timestamp;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SampleData {
            private Integer timeMsec;
            private Double distanceMm;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoadCellDTO {
        private Boolean detected;
        private Double weight;
        private Boolean isLiquid;
        private String cupType;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LiquidDTO {
        private Boolean detected;
        private Double addedWeight;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UltrasonicDTO {
        private Boolean detected;
        private Double distanceCm;
        private Double fillRate;
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventSummaryDTO {
        private Boolean isValidInput;
        private Boolean hasLiquid;
        private Boolean cupAccepted;
        private Long processingTimeMs;
    }
}
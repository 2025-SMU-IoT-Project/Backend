package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinStatusDTO {
    private Long binId;
    private String binName;
    private CurrentStatusDTO currentStatus;
    private TodayStatsDTO todayStats;
    private SensorStatusDTO sensorStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentStatusDTO {
        private Double fillRate;
        private String fillLevel;  // EMPTY, LOW, MEDIUM, HIGH, FULL
        private Double distanceCm;
        private Double totalWeight;
        private Boolean needsCollection;
        private LocalDateTime lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayStatsDTO {
        private Integer totalInputs;
        private Integer validCups;
        private Integer invalidCups;
        private Integer emptyCups;
        private Integer liquidCups;
        private Double liquidRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorStatusDTO {
        private SensorInfo irSensor;
        private SensorInfo laserSensor;
        private SensorInfo loadCell;
        private SensorInfo ultrasonic;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SensorInfo {
            private String status;  // ONLINE, OFFLINE, ERROR
            private LocalDateTime lastActive;
        }
    }
}
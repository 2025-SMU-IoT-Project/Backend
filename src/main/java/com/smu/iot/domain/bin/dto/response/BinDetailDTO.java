package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinDetailDTO {
    private Long binId;
    private String binName;
    private String binCode;
    private LocationDTO location;
    private SpecificationDTO specifications;
    private StatusDTO status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationDTO {
        private String building;
        private Integer floor;
        private String room;
        private Double latitude;
        private Double longitude;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecificationDTO {
        private Double capacity;
        private Double heightCm;
        private Double widthMm;
        private Double maxWeight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusDTO {
        private Boolean isActive;
        private Boolean isOnline;
        private LocalDateTime lastHeartbeat;
        private LocalDate installDate;
    }
}
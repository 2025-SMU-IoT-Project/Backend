package com.smu.iot.domain.bin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinUpdateRequestDTO {
    private String binName;
    private LocationDTO location;
    private SpecificationDTO specifications;

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
}
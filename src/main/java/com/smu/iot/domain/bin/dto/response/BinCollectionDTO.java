package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinCollectionDTO {
    private Integer totalBins;
    private Integer needsCollectionCount;
    private List<CollectionBinDTO> bins;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionBinDTO {
        private Long binId;
        private String binName;
        private Double fillRate;
        private Double totalWeight;
        private String priority;  // HIGH, MEDIUM, LOW
        private LocationDTO location;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class LocationDTO {
            private String building;
            private Integer floor;
            private Double latitude;
            private Double longitude;
        }
    }
}
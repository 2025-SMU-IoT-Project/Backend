package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinListDTO {
    private Long binId;
    private String binName;
    private LocationInfoDTO location;
    private StatusInfoDTO status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationInfoDTO {
        private String building;
        private Integer floor;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusInfoDTO {
        private Boolean isOnline;
        private Double fillRate;
        private Boolean needsCollection;
    }
}
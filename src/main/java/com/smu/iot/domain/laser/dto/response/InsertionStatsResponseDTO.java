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
public class InsertionStatsResponseDTO {

    private Long binId;
    private Long totalInsertions;
    private Long validCupCount;
    private Long rejectedCupCount;
    private Double validCupPercentage;
    private PatternStats patternStats;
    private List<RecentEvent> recentEvents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatternStats {
        private Long increasingCount;
        private Long decreasingCount;
        private Long constantCount;
        private Long irregularCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentEvent {
        private Long eventId;
        private LocalDateTime regDate;
        private Boolean isValidCup;
        private String patternType;
        private Double maxDiameterMm;
        private String rejectionReason;
    }
}
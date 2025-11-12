package com.smu.iot.domain.laser.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertionEventResponseDTO {

    // 이벤트 ID
    private Long eventId;

    // 쓰레기통 ID
    private Long binId;

    // 유효한 컵인지
    private Boolean isValidCup;

    // 패턴 타입
    private String patternType;

    // 패턴 설명
    private String patternDescription;

    // 최소 지름 (mm)
    private Double minDiameterMm;

    // 최대 지름 (mm)
    private Double maxDiameterMm;

    // 지름 변화량 (mm)
    private Double diameterChangeMm;

    // 샘플 수
    private Integer sampleCount;

    // 거부 사유 (유효하지 않을 때)
    private String rejectionReason;

    // 계산된 지름 데이터 (그래프용)
    private List<DiameterData> diameterData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiameterData {
        private Integer timeMsec;
        private Double diameterMm;
    }
}
package com.smu.iot.domain.ultrasonic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinFillRateResponseDTO {
    private Long binId;
    private String uuid;
    private Double distanceCm;
    private Double fillRate;
    private Double binHeight;           // 쓰레기통 높이
    private Boolean needsCollection;    // 수거 필요 여부
    private Double collectionThreshold; // 수거 임계값 (기본 80%)
    private LocalDateTime lastUpdated;  // 마지막 업데이트 시간
}
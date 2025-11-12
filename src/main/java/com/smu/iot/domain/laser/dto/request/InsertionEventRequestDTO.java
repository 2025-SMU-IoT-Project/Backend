package com.smu.iot.domain.laser.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// STM32 → Spring 서버 전송용 요청 DTO
// 1초간 20회 측정한 데이터를 한 번에 전송
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertionEventRequestDTO {

    private Long binId;
    private Double binWidthMm;
    private List<SampleData> samples;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SampleData {
        private Integer timeMsec;
        private Double distanceMm;
    }
}
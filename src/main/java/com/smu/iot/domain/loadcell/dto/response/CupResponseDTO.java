package com.smu.iot.domain.loadcell.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupResponseDTO {
    private Long recordId;      // Cup 레코드 ID
    private String uuid;        // 이벤트 고유 ID
    private Long binId;         // 쓰레기통 ID
    private Double weight;      // 투입된 컵의 무게 (g)
    private LocalDateTime timestamp;  // 기록 시간
}
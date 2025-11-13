package com.smu.iot.domain.ir.entity;

import com.smu.iot.domain.ir.entity.code.CupType;
import com.smu.iot.domain.ir.entity.code.SensorEventType;
import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ir")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ir extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // STM32에서 생성한 이벤트 고유 ID (여러 센서가 같은 UUID 공유)
    @Column(nullable = false, length = 36)
    private String uuid;

    // 센서 식별
    @Column(nullable = false, length = 10)
    private String sensorId;  // "IR1" or "IR2"

    @Column(nullable = false, length = 20)
    private String binId;  // 쓰레기통 ID

    // 센서 데이터
    @Column(nullable = false)
    private Boolean beamBlocked;  // IR 빔 차단 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SensorEventType eventType;  // ENTRY_DETECTED, CUP_TYPE_DETECTED

    // 분석 결과 (IR2에서만 사용)
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CupType cupType;  // PLASTIC, PAPER, UNKNOWN=
}
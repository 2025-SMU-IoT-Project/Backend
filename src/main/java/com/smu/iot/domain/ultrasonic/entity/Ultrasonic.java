package com.smu.iot.domain.ultrasonic.entity;

import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ultrasonic")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ultrasonic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private Long binId;

    @Column(nullable = false, length = 36)
    private String uuid; // 이벤트 고유 ID

    @Column(name = "distance_cm", nullable = false)
    private Double distanceCm;  // STM32에서 측정한 센서 거리 (cm)

    @Column(name = "fill_rate", nullable = false)
    private Double fillRate;  // 서버에서 계산한 채움률
}

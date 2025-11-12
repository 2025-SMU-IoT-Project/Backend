package com.smu.iot.domain.laser.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "laser")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Laser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "reg_date", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private InsertionEvent insertionEvent;

    @Column(name = "time_ms", nullable = false)
    private Integer timeMsec;  // 측정 시간

    @Column(name = "distance_mm", nullable = false)
    private Double distanceMm;  // STM32에서 측정한 센서 거리 (mm)

    @Column(name = "diameter_mm", nullable = false)
    private Double diameterMm;  // 서버에서 계산한 컵 지름 (mm)

    // 지름 계산 (쓰레기통 너비 - 2 × 센서 거리)
    public static double calculateDiameter(double binWidthMm, double distanceMm) {
        return binWidthMm - (2 * distanceMm);
    }
}

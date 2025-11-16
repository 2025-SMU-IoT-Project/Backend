package com.smu.iot.domain.event.entity;

import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.ir.entity.Ir;
import com.smu.iot.domain.laser.entity.CupShape;
import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id", nullable = false)
    private Bin bin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    // 센서 데이터 연관관계 (데이터가 있을 때만)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ir_id")
    private Ir irData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laser_id")
    private CupShape laserData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cup_id")
    private Cup cupData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultrasonic_id")
    private Ultrasonic ultrasonicData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquid_history_id")
    private LiquidHistory liquidHistoryData;

    // 센서 데이터 존재 여부 (빠른 조회용)
    @Column(name = "has_ir_data", nullable = false)
    private Boolean hasIrData;

    @Column(name = "has_laser_data", nullable = false)
    private Boolean hasLaserData;

    @Column(name = "has_cup_data", nullable = false)
    private Boolean hasCupData;

    @Column(name = "has_ultrasonic_data", nullable = false)
    private Boolean hasUltrasonicData;

    @Column(name = "has_liquid_data", nullable = false)
    private Boolean hasLiquidData;

    // 이벤트 결과 요약
    @Column(name = "is_valid_input", nullable = false)
    private Boolean isValidInput;

    @Column(name = "has_liquid", nullable = false)
    private Boolean hasLiquid;

    @Column(name = "cup_accepted", nullable = false)
    private Boolean cupAccepted;

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    // 타이밍 정보
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "ir_timestamp")
    private LocalDateTime irTimestamp;

    @Column(name = "laser_timestamp")
    private LocalDateTime laserTimestamp;

    @Column(name = "cup_timestamp")
    private LocalDateTime cupTimestamp;

    @Column(name = "ultrasonic_timestamp")
    private LocalDateTime ultrasonicTimestamp;

    @Column(name = "liquid_timestamp")
    private LocalDateTime liquidTimestamp;

    @Getter
    public enum EventStatus {
        INITIATED("초기화"),
        IR_DETECTED("IR 감지"),
        LASER_PROCESSING("레이저 측정중"),
        WEIGHT_MEASURING("컵 무게 측정중"),
        LIQUID_MEASURING("액체 무게 측정중"),
        ULTRASONIC_MEASURING("채움률 측정중"),
        COMPLETED("완료"),
        REJECTED("거부됨"),
        ERROR("오류"),
        TIMEOUT("타임아웃");

        private final String description;

        EventStatus(String description) {
            this.description = description;
        }
    }

    public void calculateProcessingTime() {
        if (this.startTime != null && this.endTime != null) {
            this.processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public boolean isAllSensorDataReceived() {
        return hasIrData && hasLaserData && hasCupData && hasUltrasonicData;
    }

    public void completeEvent() {
        this.status = cupAccepted ? EventStatus.COMPLETED : EventStatus.REJECTED;
        this.endTime = LocalDateTime.now();
        calculateProcessingTime();
    }

    public void linkIrData(Ir ir) {
        this.irData = ir;
        this.hasIrData = true;
        this.irTimestamp = LocalDateTime.now();
        if (this.startTime == null) {
            this.startTime = this.irTimestamp;
        }
    }

    public void linkLaserData(CupShape cupShape) {
        this.laserData = cupShape;
        this.hasLaserData = true;
        this.laserTimestamp = LocalDateTime.now();
    }

    public void linkCupData(Cup cup) {
        this.cupData = cup;
        this.hasCupData = true;
        this.cupTimestamp = LocalDateTime.now();
    }

    public void linkUltrasonicData(Ultrasonic ultrasonic) {
        this.ultrasonicData = ultrasonic;
        this.hasUltrasonicData = true;
        this.ultrasonicTimestamp = LocalDateTime.now();
    }

    public void linkLiquidHistoryData(LiquidHistory liquidHistory) {
        this.liquidHistoryData = liquidHistory;
        this.hasLiquidData = true;
        this.liquidTimestamp = LocalDateTime.now();
    }

    public Long getBinId() {
        return this.bin != null ? this.bin.getId() : null;
    }
}
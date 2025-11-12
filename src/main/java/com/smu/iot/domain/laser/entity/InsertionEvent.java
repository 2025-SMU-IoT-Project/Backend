package com.smu.iot.domain.laser.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "insertion_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "reg_date", nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(name = "bin_id", nullable = false)
    private Long binId;  // 쓰레기통 ID

    @Column(name = "bin_width_mm", nullable = false)
    private Double binWidthMm;  // 쓰레기통 너비 (mm)

    @Column(name = "is_valid_cup", nullable = false)
    private Boolean isValidCup;  // 유효한 컵인지 여부

    @Column(name = "pattern_type", length = 20)
    @Enumerated(EnumType.STRING)
    private PatternType patternType;  // 패턴 타입

    @Column(name = "min_diameter_mm")
    private Double minDiameterMm;  // 최소 지름 (하단)

    @Column(name = "max_diameter_mm")
    private Double maxDiameterMm;  // 최대 지름 (상단)

    @Column(name = "diameter_change_mm")
    private Double diameterChangeMm;  // 지름 변화량

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;  // 거부 사유

    @Column(name = "sample_count")
    private Integer sampleCount;  // 측정 샘플 수

    @OneToMany(mappedBy = "insertionEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Laser> measurements = new ArrayList<>();

    public void addMeasurement(Laser measurement) {
        measurements.add(measurement);
        measurement.setInsertionEvent(this);
    }

    @Getter
    public enum PatternType {
        NORMAL("정상 컵"),
        ABNORMAL("뒤집힌 컵"),
        CONSTANT("캔/병"),
        IRREGULAR("비정상");

        private final String description;

        PatternType(String description) {
            this.description = description;
        }

    }
}


package com.smu.iot.domain.loadcell.entity;

import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bin_weight")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinWeight extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bin_id", nullable = false, unique = true)
    private Long binId;  // 쓰레기통 ID (유니크)

    @Column(name = "current_weight", nullable = false)
    private Double currentWeight;  // 현재 컵통의 총 무게 (g)

    @Column(name = "previous_weight")
    private Double previousWeight;  // 이전 측정 무게 (g)

    @Column(name = "tare_weight")
    private Double tareWeight;  // 빈 컵통의 무게 (영점, g)

    @Column(name = "is_initialized", nullable = false)
    @Builder.Default
    private Boolean isInitialized = false;  // 초기화 여부
}
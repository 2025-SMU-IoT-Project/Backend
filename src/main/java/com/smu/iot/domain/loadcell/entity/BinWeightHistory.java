package com.smu.iot.domain.loadcell.entity;

import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bin_weight_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinWeightHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bin_id", nullable = false)
    private Long binId;  // 쓰레기통 ID

    @Column(name = "current_weight", nullable = false)
    private Double currentWeight;  // 측정 시점의 컵통 총 무게 (g)

    @Column(name = "added_weight", nullable = false)
    private Double addedWeight;  // 이번에 추가된 무게 (= 투입된 컵 무게) (g)

    @Column(name = "uuid", length = 36)
    private String uuid;  // 연관된 컵 투입 이벤트 UUID

    @Column(name = "cup_id")
    private Long cupId;  // 연관된 Cup 레코드 ID
}
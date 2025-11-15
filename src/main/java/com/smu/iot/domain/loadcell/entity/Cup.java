package com.smu.iot.domain.loadcell.entity;

import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이벤트 고유 ID
    @Column(nullable = false, length = 36)
    private String uuid;

    // 쓰레기통 ID
    @Column(name = "bin_id", nullable = false)
    private Long binId;

    // 측정 데이터
    @Column(name = "weight", nullable = false)
    private Double weight;  // 측정된 무게 (g)

    @Column(name = "is_liquid", nullable = false)
    private Boolean isLiquid;  // 액체 포함 여부

    // 분석 결과
    @Enumerated(EnumType.STRING)
    @Column(name = "cup_type", nullable = false, length = 20)
    private CupWeightType cupType;

    @Column(name = "base_weight")
    private Double baseWeight;  // 영점 무게 (g)

    @Column(name = "liquid_weight")
    private Double liquidWeight;  // 액체 무게 (실제 무게 - 기준 무게)

    // 임계값 정보(?)
    @Column(name = "weight_threshold")
    private Double weightThreshold;  // 무게 감지 임계값

    @Column(name = "liquid_threshold")
    private Double liquidThreshold;  // 액체 판별 임계값

    @Getter
    public enum CupWeightType {
        EMPTY_CUP("빈 컵", 4.0, 7.0),
        LIGHT_LIQUID("약간의 액체", 20.0, 100.0),
        MEDIUM_LIQUID("중간 액체", 100.0, 200.0),
        HEAVY_LIQUID("많은 액체", 200.0, 350.0),
        ABNORMAL("비정상 무게", 0.0, 0.0);

        private final String description;
        private final Double minWeight;
        private final Double maxWeight;

        CupWeightType(String description, Double minWeight, Double maxWeight) {
            this.description = description;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
        }

        // 무게로 타입 판별
        public static CupWeightType fromWeight(Double weight) {
            if (weight >= EMPTY_CUP.minWeight && weight <= EMPTY_CUP.maxWeight) {
                return EMPTY_CUP;
            } else if (weight >= LIGHT_LIQUID.minWeight && weight < LIGHT_LIQUID.maxWeight) {
                return LIGHT_LIQUID;
            } else if (weight >= MEDIUM_LIQUID.minWeight && weight < MEDIUM_LIQUID.maxWeight) {
                return MEDIUM_LIQUID;
            } else if (weight >= HEAVY_LIQUID.minWeight && weight <= HEAVY_LIQUID.maxWeight) {
                return HEAVY_LIQUID;
            }
            return ABNORMAL;
        }
    }
}
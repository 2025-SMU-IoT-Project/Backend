package com.smu.iot.domain.cup.entity;

import com.smu.iot.domain.bin.entity.Bin;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Cup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cup_id")
    private Long id;

    // 물 + 컵 무게
    private double weightFirst;

    // 액체 무게 (원래 컵 무게와 액체 버리고 난 다음 컵 무게의 차)
    private double liquidWeight;

    // 액체 감지 여부
    private Boolean hasLiquid;

    // 기타 센서값 추가...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;
}

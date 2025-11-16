package com.smu.iot.domain.liquid.entitiy;

import com.smu.iot.domain.bin.entity.Bin;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Liquid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liquid_id")
    private Long id;

    private double weight;

    // 센서 데이터가 수집(수정)된 시각
    @LastModifiedDate
    private LocalDateTime measuredAt;

    // 이전 측정값과 비교해 새로 추가된 물의 양
    private double addedWeight;

    // 4kg 초과(혹은 80% 이상 찼는지) 여부
    private Boolean overloaded = false;

    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @Column(name = "last_uuid", length = 36)
    private String lastUuid;

    @OneToOne
    @JoinColumn(name = "bin_id")
    private Bin bin;

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    public void update(double weight, double addedWeight, boolean overloaded, LocalDateTime measuredAt, String uuid) {
        this.lastUuid = this.uuid;
        this.uuid = uuid;
        this.weight = weight;
        this.addedWeight = addedWeight;
        this.overloaded = overloaded;
        this.measuredAt = measuredAt;
    }
}

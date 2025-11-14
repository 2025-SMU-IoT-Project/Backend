package com.smu.iot.domain.liquid.entitiy;

import com.smu.iot.domain.bin.entity.Bin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class LiquidHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double weight;

    private double addedWeight;

    private LocalDateTime measuredAt;

    private Boolean overload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquid_id")
    private Liquid liquid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id")
    private Bin bin;
}

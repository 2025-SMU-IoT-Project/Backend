package com.smu.iot.domain.bin.entity;

import com.smu.iot.domain.cup.entity.Cup;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Bin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bin_id")
    private Long id;

    private String name;

    private String location;

    @OneToOne(mappedBy = "bin")
    private Liquid liquid;

    @OneToMany(mappedBy = "bin")
    private List<Cup> cups = new ArrayList<>();
}

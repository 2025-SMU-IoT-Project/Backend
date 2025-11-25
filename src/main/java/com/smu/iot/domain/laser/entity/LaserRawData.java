package com.smu.iot.domain.laser.entity;

import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "laser_raw_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaserRawData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @ElementCollection
    @CollectionTable(name = "laser_raw_data_values", joinColumns = @JoinColumn(name = "laser_raw_data_id"))
    @Column(name = "data_value")
    private List<Integer> data;
}

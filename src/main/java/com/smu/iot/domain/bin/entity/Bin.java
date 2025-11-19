package com.smu.iot.domain.bin.entity;

import com.smu.iot.domain.event.entity.Event;
import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bin_code", nullable = false, unique = true, length = 20)
    private String binCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "building", nullable = false, length = 50)
    private String building;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "capacity", nullable = false)
    private Double capacity;

    @Column(name = "height_cm", nullable = false)
    private Double heightCm;

    @Column(name = "width_mm", nullable = false)
    private Double widthMm;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BinStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "fill_threshold")
    private Double fillThreshold;

    @Column(name = "weight_threshold")
    private Double weightThreshold;

    @Column(name = "current_weight")
    @Builder.Default
    private Double currentWeight = 0.0;

    @Column(name = "current_liquid_weight")
    @Builder.Default
    private Double currentLiquidWeight = 0.0;

    @OneToMany(mappedBy = "bin", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    @Getter
    public enum BinStatus {
        NORMAL("정상"),
        WARNING("경고"),
        FULL("가득참"),
        OFFLINE("오프라인"),
        MAINTENANCE("유지보수중");

        private final String description;

        BinStatus(String description) {
            this.description = description;
        }
    }

    public void updateStatus(BinStatus newStatus) {
        this.status = newStatus;
    }

    public void updateOnlineStatus(Boolean isOnline) {
        this.isOnline = isOnline;
        if (!isOnline) {
            this.status = BinStatus.OFFLINE;
        }
    }

    public void updateFillLevel(Double fillRate) {
        this.capacity = fillRate;

        if (fillRate >= (fillThreshold != null ? fillThreshold : 80.0)) {
            this.status = BinStatus.FULL;
        } else if (fillRate >= 70.0) {
            this.status = BinStatus.WARNING;
        } else {
            this.status = BinStatus.NORMAL;
        }
    }

    public void addWeight(Double weight) {
        if (this.currentWeight == null) {
            this.currentWeight = 0.0;
        }
        this.currentWeight += weight;
    }

    public void updateLiquidWeight(Double weight) {
        this.currentLiquidWeight = weight;
    }
}
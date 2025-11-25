package com.smu.iot.domain.laser.repository;

import com.smu.iot.domain.laser.entity.LaserRawData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaserRawDataRepository extends JpaRepository<LaserRawData, Long> {
    long countByUuid(String uuid);

    List<LaserRawData> findAllByUuid(String uuid);

    void deleteAllByUuid(String uuid);
}

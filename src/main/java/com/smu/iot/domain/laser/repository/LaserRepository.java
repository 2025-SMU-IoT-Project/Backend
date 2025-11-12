package com.smu.iot.domain.laser.repository;

import com.smu.iot.domain.laser.entity.Laser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaserRepository extends JpaRepository<Laser, Long> {
}

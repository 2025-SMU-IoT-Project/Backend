package com.smu.iot.domain.ultrasonic.repository;

import com.smu.iot.domain.ultrasonic.entity.Ultrasonic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UltrasonicRepository extends JpaRepository<Ultrasonic, Long> {

    Optional<Ultrasonic> findByUuid(String uuid);

    Optional<Ultrasonic> findFirstByBinIdOrderByCreatedAtDesc(Long binId);

    List<Ultrasonic> findByBinIdOrderByCreatedAtDesc(Long binId);

    List<Ultrasonic> findTop5ByBinIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long binId, LocalDateTime start, LocalDateTime end);

    List<Ultrasonic> findTop5ByBinIdOrderByCreatedAtDesc(Long binId);
}
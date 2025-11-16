package com.smu.iot.domain.event.repository;

import com.smu.iot.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByUuid(String uuid);

    List<Event> findByBin_IdOrderByCreatedAtDesc(Long binId);

    @Query("SELECT e FROM Event e WHERE e.bin.id = :binId " +
        "AND e.createdAt BETWEEN :startDate AND :endDate " +
        "ORDER BY e.createdAt DESC")
    List<Event> findByBinIdAndDateRange(
        @Param("binId") Long binId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
package com.smu.iot.domain.event.repository;

import com.smu.iot.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByUuid(String uuid);

    List<Event> findByBin_IdOrderByCreatedAtDesc(Long binId);
}
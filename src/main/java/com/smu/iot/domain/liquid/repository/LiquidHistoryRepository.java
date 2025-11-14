package com.smu.iot.domain.liquid.repository;


import com.smu.iot.domain.liquid.entitiy.LiquidHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LiquidHistoryRepository extends JpaRepository<LiquidHistory, Long> {
    List<LiquidHistory> findByBinIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(Long binId, LocalDateTime start, LocalDateTime end);
}

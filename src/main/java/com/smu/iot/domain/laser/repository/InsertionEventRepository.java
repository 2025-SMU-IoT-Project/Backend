package com.smu.iot.domain.laser.repository;

import com.smu.iot.domain.laser.entity.InsertionEvent;
import com.smu.iot.domain.laser.entity.InsertionEvent.PatternType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsertionEventRepository extends JpaRepository<InsertionEvent, Long> {

    Long countByBinId(Long binId);

    Long countByBinIdAndIsValidCupTrue(Long binId);

    Long countByBinIdAndPatternType(Long binId, PatternType patternType);

    List<InsertionEvent> findTop10ByBinIdOrderByRegDateDesc(Long binId);

}


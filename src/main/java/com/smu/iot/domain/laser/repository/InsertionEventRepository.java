package com.smu.iot.domain.laser.repository;

import com.smu.iot.domain.laser.entity.CupShape;
import com.smu.iot.domain.laser.entity.CupShape.PatternType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsertionEventRepository extends JpaRepository<CupShape, Long> {

    Long countByBinId(Long binId);

    Long countByBinIdAndIsValidCupTrue(Long binId);

    Long countByBinIdAndPatternType(Long binId, PatternType patternType);

    List<CupShape> findTop10ByBinIdOrderByRegDateDesc(Long binId);

    Optional<CupShape> findByUuid(String uuid);

}


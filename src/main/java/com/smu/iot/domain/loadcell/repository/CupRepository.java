package com.smu.iot.domain.loadcell.repository;

import com.smu.iot.domain.loadcell.entity.Cup;
import com.smu.iot.domain.loadcell.entity.Cup.CupWeightType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CupRepository extends JpaRepository<Cup, Long> {

    Optional<Cup> findByUuid(String uuid);

    List<Cup> findByBinIdOrderByCreatedAtDesc(Long binId);

    Long countByBinId(Long binId);

    Long countByBinIdAndCupType(Long binId, CupWeightType cupType);

    @Query("SELECT MAX(c.weight) FROM Cup c WHERE c.binId = :binId")
    Double getMaxWeight(@Param("binId") Long binId);

    // 액체가 포함된 타입들의 평균 무게 조회
    @Query("SELECT AVG(c.weight) FROM Cup c WHERE c.binId = :binId AND c.cupType IN :cupTypes")
    Double getAverageWeightByCupTypes(@Param("binId") Long binId, @Param("cupTypes") List<CupWeightType> cupTypes);

    // 액체가 포함된 타입들의 총 액체 무게 합산
    @Query("SELECT SUM(c.liquidWeight) FROM Cup c WHERE c.binId = :binId AND c.cupType IN :cupTypes")
    Double getTotalLiquidWeightByCupTypes(@Param("binId") Long binId, @Param("cupTypes") List<CupWeightType> cupTypes);
}
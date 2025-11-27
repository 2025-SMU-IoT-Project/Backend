package com.smu.iot.domain.loadcell.repository;

import com.smu.iot.domain.loadcell.entity.BinWeightHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BinWeightHistoryRepository extends JpaRepository<BinWeightHistory, Long> {

    // binId로 최근 히스토리 조회
    List<BinWeightHistory> findByBinIdOrderByCreatedAtDesc(Long binId);

    // 시간 범위로 조회 (그래프용)
    @Query("SELECT h FROM BinWeightHistory h WHERE h.binId = :binId AND h.createdAt BETWEEN :startTime AND :endTime ORDER BY h.createdAt ASC")
    List<BinWeightHistory> findByBinIdAndTimeRange(
        @Param("binId") Long binId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    List<BinWeightHistory> findByBinIdAndUuidOrderByCreatedAtDesc(Long binId, String uuid, Pageable pageable);

    List<BinWeightHistory> findByBinIdOrderByCreatedAtDesc(Long binId, Pageable pageable);
}
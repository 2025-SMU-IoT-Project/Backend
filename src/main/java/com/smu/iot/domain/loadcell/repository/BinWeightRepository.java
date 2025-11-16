package com.smu.iot.domain.loadcell.repository;

import com.smu.iot.domain.loadcell.entity.BinWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BinWeightRepository extends JpaRepository<BinWeight, Long> {

    Optional<BinWeight> findByBinId(Long binId);
}
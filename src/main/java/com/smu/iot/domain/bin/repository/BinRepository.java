package com.smu.iot.domain.bin.repository;

import com.smu.iot.domain.bin.entity.Bin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinRepository extends JpaRepository<Bin, Long> {
}

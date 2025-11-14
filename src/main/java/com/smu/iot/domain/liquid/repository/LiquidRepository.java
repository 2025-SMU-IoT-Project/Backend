package com.smu.iot.domain.liquid.repository;

import com.smu.iot.domain.bin.entity.Bin;
import com.smu.iot.domain.liquid.entitiy.Liquid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface LiquidRepository extends JpaRepository<Liquid, Long> {
    Optional<Liquid> findByBin(Bin bin);

    List<Liquid> findAllByOverloaded(Boolean overloaded);
}

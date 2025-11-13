package com.smu.iot.domain.ir.repository;

import com.smu.iot.domain.ir.entity.CupInputRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CupInputRecordRepository extends JpaRepository<CupInputRecord, Long> {

}
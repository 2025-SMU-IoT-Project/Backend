package com.smu.iot.domain.ir.repository;

import com.smu.iot.domain.ir.entity.Ir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrSensorEventRepository extends JpaRepository<Ir, Long> {

    // UUID로 IR 이벤트 조회 (IR1, IR2)
    List<Ir> findByUuid(String uuid);

    // 특정 쓰레기통의 최근 이벤트 조회
    List<Ir> findByBinIdOrderByCreatedAtDesc(String binId);

}
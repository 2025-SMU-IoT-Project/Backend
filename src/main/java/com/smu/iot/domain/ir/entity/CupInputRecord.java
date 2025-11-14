package com.smu.iot.domain.ir.entity;

import com.smu.iot.domain.ir.entity.code.CupType;
import com.smu.iot.domain.ir.entity.code.InputStatus;
import com.smu.iot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cup_input_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupInputRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String uuid;

    @Column(nullable = false, length = 20)
    private String binId;

    // 투입 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CupType cupType;  // PLASTIC, PAPER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InputStatus status;  // ACCEPTED, REJECTED

    @Column(length = 200)
    private String rejectionReason;  // 거절 사유

    // IR2 이벤트 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ir2_event_id")
    private Ir ir2Event;
}
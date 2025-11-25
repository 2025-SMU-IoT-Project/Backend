package com.smu.iot.domain.laser.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaserPacketRequestDTO {
    private String uuid;
    private Integer idx;
    private List<Integer> data;
}

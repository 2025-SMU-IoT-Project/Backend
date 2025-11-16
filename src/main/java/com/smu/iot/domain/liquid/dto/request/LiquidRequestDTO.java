package com.smu.iot.domain.liquid.dto.request;

import lombok.Data;

public class LiquidRequestDTO {

    @Data
    public static class CreateLiquidDTO {
        private double weight;
    }

    @Data
    public static class UpdateLiquidDTO {
        private double weight;
        private String uuid;
    }
}

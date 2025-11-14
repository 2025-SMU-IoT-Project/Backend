package com.smu.iot.domain.liquid.dto.request;

import lombok.Getter;

public class LiquidRequestDTO {

    @Getter
    public static class CreateLiquidDTO {
        private double weight;
    }

    @Getter
    public static class UpdateLiquidDTO {
        private double weight;
    }
}

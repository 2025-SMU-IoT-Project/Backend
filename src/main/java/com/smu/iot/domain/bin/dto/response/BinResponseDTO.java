package com.smu.iot.domain.bin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BinResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateBinResultDTO {
        private Long id;
        private String name;
        private String location;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BinPreviewDTO {
        private Long id;
        private String name;
        private String location;
    }
}

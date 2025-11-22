package com.smu.iot.global.apipayload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "커서 기반 페이지네이션 응답")
public class CursorResult<T> {
    @Schema(description = "조회된 데이터 목록")
    private List<T> values;

    @Schema(description = "다음 커서 ID (없으면 null)")
    private Long nextCursor;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}

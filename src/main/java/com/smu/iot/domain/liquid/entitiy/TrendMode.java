package com.smu.iot.domain.liquid.entitiy;

public enum TrendMode {
    TREND,      // 단순 트렌드 (그 시점 weight 그대로)
    CUMULATIVE  // 누적합 (addedWeight 누적)
}

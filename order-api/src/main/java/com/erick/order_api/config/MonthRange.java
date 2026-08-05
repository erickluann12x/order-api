package com.erick.order_api.config;

import java.time.Instant;

public record MonthRange(
        Instant start,
        Instant end
) {
}

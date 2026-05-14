package com.estapar.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueResponse(BigDecimal amount, String currency, Instant timestamp) {
}

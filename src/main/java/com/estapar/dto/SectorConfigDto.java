package com.estapar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record SectorConfigDto(
        @JsonProperty("sector") String sector,
        @JsonProperty("basePrice") BigDecimal basePrice,
        @JsonProperty("max_capacity") Integer maxCapacity
) {
}

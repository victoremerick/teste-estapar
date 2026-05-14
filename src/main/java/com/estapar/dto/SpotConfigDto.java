package com.estapar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotConfigDto(
        @JsonProperty("id") Long id,
        @JsonProperty("sector") String sector,
        @JsonProperty("lat") Double lat,
        @JsonProperty("lng") Double lng
) {
}

package com.estapar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GarageConfigResponse(
        @JsonProperty("garage") List<SectorConfigDto> garage,
        @JsonProperty("spots") List<SpotConfigDto> spots
) {
}

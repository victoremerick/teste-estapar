package com.estapar.dto;

import com.estapar.enums.EventType;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Instant;

public record WebhookEventRequest(
        @JsonAlias({"event_type", "eventType"}) EventType eventType,
        @JsonAlias({"license_plate", "plate", "licensePlate"}) String licensePlate,
        String sector,
        @JsonAlias({"spot", "vacancy"}) String spot,
        @JsonAlias({"timestamp", "entry_at", "entry_time", "parked_at", "exit_at", "exit_time"}) Instant timestamp,
        Double lat,
        Double lng
) {
}

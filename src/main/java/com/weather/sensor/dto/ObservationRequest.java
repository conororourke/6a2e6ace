package com.weather.sensor.dto;

import com.weather.sensor.model.Observation;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ObservationRequest(
        @NotNull Instant recordedAt,

        @DecimalMin("-100")
        @DecimalMax("100")
        Double temperature,

        @DecimalMin("0")
        @DecimalMax("100")
        Double humidity,

        @DecimalMin("0")
        Double windSpeed,

        @DecimalMin("850")
        @DecimalMax("1100")
        Double pressure,

        @DecimalMin("0")
        Double rainfall
) {

    @AssertTrue(message = "at least one observation metric must exist")
    public boolean isAtLeastOneObservationMetricPresent() {
        return temperature != null || humidity != null || windSpeed != null
                || pressure != null || rainfall != null;
    }

    public Observation toObservation(long sensorId, Instant receivedAt) {
        return new Observation(sensorId, recordedAt, receivedAt,
                temperature, humidity, windSpeed, pressure, rainfall);
    }
}
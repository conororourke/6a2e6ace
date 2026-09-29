package com.weather.sensor.model;

import java.time.Instant;

public record Observation(
        long sensorId,
        Instant recordedAt,
        Instant receivedAt,
        Double temperature,
        Double humidity,
        Double windSpeed,
        Double pressure,
        Double rainfall) {
}
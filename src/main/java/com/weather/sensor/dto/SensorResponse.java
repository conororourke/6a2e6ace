package com.weather.sensor.dto;

import com.weather.sensor.model.Sensor;

import java.math.BigDecimal;
import java.time.Instant;

public record SensorResponse(Long id, String name, BigDecimal latitude,
                             BigDecimal longitude, Instant createdAt) {

    public static SensorResponse from(Sensor sensor) {
        return new SensorResponse(sensor.id(), sensor.name(), sensor.latitude(),
                sensor.longitude(), sensor.createdAt());
    }
}

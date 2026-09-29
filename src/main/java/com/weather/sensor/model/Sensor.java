package com.weather.sensor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("sensor")
public record Sensor(
        @Id Long id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt) {
}
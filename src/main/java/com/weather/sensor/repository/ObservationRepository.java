package com.weather.sensor.repository;

import com.weather.sensor.model.Observation;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
public class ObservationRepository {

    private final JdbcClient jdbc;

    public ObservationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns true if inserted, false if an observation already existed for that sensor and time. */
    public boolean insert(Observation o) {
        int rows = jdbc.sql("""
            INSERT INTO observation
                (sensor_id, recorded_at, received_at, temperature, humidity, wind_speed, pressure, rainfall)
            VALUES
                (:sensorId, :recordedAt, :receivedAt, :temperature, :humidity, :windSpeed, :pressure, :rainfall)
            ON CONFLICT (sensor_id, recorded_at) DO NOTHING
            """)
                .param("sensorId", o.sensorId())
                .param("recordedAt", toUtc(o.recordedAt()))
                .param("receivedAt", toUtc(o.receivedAt()))
                .param("temperature", o.temperature())
                .param("humidity", o.humidity())
                .param("windSpeed", o.windSpeed())
                .param("pressure", o.pressure())
                .param("rainfall", o.rainfall())
                .update();
        return rows == 1;
    }

    private static OffsetDateTime toUtc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
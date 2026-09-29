package com.weather.sensor.repository;

import com.weather.sensor.model.MetricAggregate;
import com.weather.sensor.model.WeatherMetric;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class StatsRepository {

    private final JdbcClient jdbc;

    public StatsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    private static final String AGGREGATES_SQL = """
            SELECT sensor_id,
                   MIN(temperature) AS temperature_min,
                   MAX(temperature) AS temperature_max,
                   AVG(temperature) AS temperature_avg,
                   MIN(humidity)    AS humidity_min,
                   MAX(humidity)    AS humidity_max,
                   AVG(humidity)    AS humidity_avg,
                   MIN(wind_speed)  AS wind_speed_min,
                   MAX(wind_speed)  AS wind_speed_max,
                   AVG(wind_speed)  AS wind_speed_avg,
                   MIN(pressure)    AS pressure_min,
                   MAX(pressure)    AS pressure_max,
                   AVG(pressure)    AS pressure_avg,
                   MIN(rainfall)    AS rainfall_min,
                   MAX(rainfall)    AS rainfall_max,
                   SUM(rainfall)    AS rainfall_sum,
                   AVG(rainfall)    AS rainfall_avg
            FROM observation
            WHERE sensor_id IN (:sensorIds)
              AND recorded_at >= :from AND recorded_at < :toExclusive
            GROUP BY sensor_id
            ORDER BY sensor_id
            """;

    /** sensor ID -> metric name -> aggregates, for sensors with observations in [from, toExclusive). */
    public Map<Long, Map<WeatherMetric, MetricAggregate>> aggregate(List<Long> sensorIds, Instant from, Instant toExclusive) {
        Map<Long, Map<WeatherMetric, MetricAggregate>> results = new LinkedHashMap<>();
        jdbc.sql(AGGREGATES_SQL)
                .param("sensorIds", sensorIds)
                .param("from", from.atOffset(ZoneOffset.UTC))
                .param("toExclusive", toExclusive.atOffset(ZoneOffset.UTC))
                .query(rs -> {
                    Map<WeatherMetric, MetricAggregate> metrics = new HashMap<>();
                    for (WeatherMetric weatherMetric : WeatherMetric.values()) {
                        String column = weatherMetric.column();
                        metrics.put(weatherMetric, new MetricAggregate(
                                rs.getObject(column + "_min", Double.class),
                                rs.getObject(column + "_max", Double.class),
                                (weatherMetric == WeatherMetric.RAINFALL) ? rs.getObject(column + "_sum", Double.class) : null,
                                rs.getObject(column + "_avg", Double.class)));
                    }
                    results.put(rs.getLong("sensor_id"), metrics);
                });
        return results;
    }

}
package com.weather.sensor.service;

import com.weather.sensor.dto.StatsResponse;
import com.weather.sensor.exception.InvalidRequestException;
import com.weather.sensor.exception.SensorNotFoundException;
import com.weather.sensor.model.MetricAggregate;
import com.weather.sensor.model.Statistic;
import com.weather.sensor.model.WeatherMetric;
import com.weather.sensor.repository.SensorRepository;
import com.weather.sensor.repository.StatsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatsService {

    private static final int MAX_RANGE_DAYS = 31;

    private final StatsRepository statsRepository;
    private final SensorRepository sensorRepository;
    private final Clock clock;

    public StatsService(StatsRepository statsRepository, SensorRepository sensorRepository, Clock clock) {
        this.statsRepository = statsRepository;
        this.sensorRepository = sensorRepository;
        this.clock = clock;
    }

    public StatsResponse query(List<WeatherMetric> weatherMetrics, List<Statistic> statistics, List<Long> sensorIds,
                               LocalDate from, LocalDate to) {

        validateMetricParams(weatherMetrics, statistics);

        LocalDate today = LocalDate.now(clock);
        if (from == null && to == null) {
            from = today;
            to = today;
        }
        validateRange(from, to, today);

        List<Long> allIds = sensorRepository.findAllIds();
        List<Long> ids = (sensorIds == null || sensorIds.isEmpty()) ? allIds : sensorIds;
        requireExistingSensors(ids, allIds);

        Map<Long, Map<WeatherMetric, MetricAggregate>> aggregates = statsRepository.aggregate(
                ids,
                from.atStartOfDay(ZoneOffset.UTC).toInstant(),
                to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        );

        return new StatsResponse(from, to, toResults(aggregates, weatherMetrics, statistics));
    }

    private static List<StatsResponse.StatResult> toResults(Map<Long, Map<WeatherMetric, MetricAggregate>> aggregates,
                                                            List<WeatherMetric> metrics,
                                                            List<Statistic> statistics) {
        List<StatsResponse.StatResult> results = new ArrayList<>();
        for (var sensor : aggregates.entrySet()) {
            for (WeatherMetric weatherMetric : metrics) {
                for (Statistic statistic : statistics) {
                    Double value = round(sensor.getValue().get(weatherMetric).value(statistic));
                    results.add(new StatsResponse.StatResult(sensor.getKey(), weatherMetric, statistic, value));
                }
            }
        }
        return results;
    }

    private static void validateMetricParams(List<WeatherMetric> weatherMetrics, List<Statistic> statistics) {
        if (statistics.contains(Statistic.SUM)
                && weatherMetrics.stream().anyMatch(metric -> metric != WeatherMetric.RAINFALL)) {
            throw new InvalidRequestException("statistics", "SUM is only supported for RAINFALL");
        }
    }

    private static void validateRange(LocalDate from, LocalDate to, LocalDate today) {
        if (from == null || to == null) {
            throw new InvalidRequestException("dateRange", "from and to must be provided together");
        }
        if (from.isAfter(to)) {
            throw new InvalidRequestException("from", "must not be after to");
        }
        if (to.isAfter(today)) {
            throw new InvalidRequestException("to", "must not be in the future");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidRequestException("to", "range must not exceed " + MAX_RANGE_DAYS + " days");
        }
    }

    private static void requireExistingSensors(List<Long> requested, List<Long> existing) {
        List<Long> missing = requested.stream()
                .filter(id -> !existing.contains(id))
                .distinct()
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw SensorNotFoundException.byIds(missing);
        }
    }

    private static Double round(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
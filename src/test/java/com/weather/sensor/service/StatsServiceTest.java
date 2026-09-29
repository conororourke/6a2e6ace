package com.weather.sensor.service;

import com.weather.sensor.dto.StatsResponse;
import com.weather.sensor.dto.StatsResponse.StatResult;
import com.weather.sensor.exception.InvalidRequestException;
import com.weather.sensor.exception.SensorNotFoundException;
import com.weather.sensor.model.MetricAggregate;
import com.weather.sensor.repository.SensorRepository;
import com.weather.sensor.repository.StatsRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.weather.sensor.model.Statistic.*;
import static com.weather.sensor.model.WeatherMetric.TEMPERATURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatsServiceTest {

    private static final LocalDate TODAY = LocalDate.parse("2026-09-29");

    private final StatsRepository statsRepository = mock(StatsRepository.class);
    private final SensorRepository sensorRepository = mock(SensorRepository.class);
    private final StatsService service = new StatsService(statsRepository, sensorRepository,
            Clock.fixed(Instant.parse("2026-09-29T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void nullDateAndNullSensorParams() {
        when(sensorRepository.findAllIds()).thenReturn(List.of(1L, 2L, 3L));
        when(statsRepository.aggregate(any(), any(), any())).thenReturn(Map.of());

        StatsResponse response = service.query(List.of(TEMPERATURE), List.of(AVG), List.of(), null, null);

        // null dates should default to today. Empty list of sensors should use all sensors
        assertEquals(TODAY, response.from());
        assertEquals(TODAY, response.to());
        verify(statsRepository).aggregate(
                List.of(1L, 2L, 3L),
                Instant.parse("2026-09-29T00:00:00Z"),
                Instant.parse("2026-09-30T00:00:00Z")
        );
    }

    @Test
    void returnsAllRequestedSensorMetricsAndStatistics() {

        when(sensorRepository.findAllIds()).thenReturn(List.of(1L, 2L));

        when(statsRepository.aggregate(any(), any(), any())).thenReturn(new TreeMap<>(Map.of(
                1L, Map.of(TEMPERATURE, new MetricAggregate(8.2, 15.1, 250.0, 12.5)),
                2L, Map.of(TEMPERATURE, new MetricAggregate(9.2, 16.1, 250.0, 13.5))
                )));

        StatsResponse response = service.query(List.of(TEMPERATURE), List.of(MIN, MAX), List.of(1L, 2L), null, null);

        assertEquals(4, response.results().size());
        assertEquals(List.of(
                new StatResult(1L, TEMPERATURE, MIN, 8.2),
                new StatResult(1L, TEMPERATURE, MAX, 15.1),
                new StatResult(2L, TEMPERATURE, MIN, 9.2),
                new StatResult(2L, TEMPERATURE, MAX, 16.1)
                ),
                response.results());
    }

    @Test
    void rejectPartialDateRangeMissingToDate() {
        assertThrows(InvalidRequestException.class, () -> service.query(List.of(TEMPERATURE), List.of(AVG), null, TODAY, null));
    }

    @Test
    void rejectPartialDateRangeMissingFromDate() {
        assertThrows(InvalidRequestException.class, () -> service.query(List.of(TEMPERATURE), List.of(AVG), null, null, TODAY));
    }

    @Test
    void rejectDateRangeGreaterThan31Days() {
        assertThrows(InvalidRequestException.class, () -> service.query(List.of(TEMPERATURE), List.of(AVG), null, TODAY.minusDays(41), TODAY.minusDays(10)));
    }

    @Test
    void acceptDateRange31Days() {
        service.query(List.of(TEMPERATURE), List.of(AVG), null, TODAY.minusDays(40), TODAY.minusDays(10));
    }

    @Test
    void rejectsUnknownSensor() {
        when(sensorRepository.findAllIds()).thenReturn(List.of(1L, 2L, 3L));
        SensorNotFoundException sensorNotFoundException = assertThrows(SensorNotFoundException.class, () -> service.query(List.of(TEMPERATURE), List.of(AVG), List.of(1L, 98L, 99L), null, null));
        assertEquals("Sensors [98, 99] not found", sensorNotFoundException.getBody().getDetail());
    }

}
package com.weather.sensor.controller;

import com.weather.sensor.dto.StatsResponse;
import com.weather.sensor.model.Statistic;
import com.weather.sensor.model.WeatherMetric;
import com.weather.sensor.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public StatsResponse query(
            @RequestParam List<WeatherMetric> weatherMetrics,
            @RequestParam(defaultValue = "AVG") List<Statistic> statistics,
            @RequestParam(required = false) List<Long> sensorIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.query(weatherMetrics, statistics, sensorIds, from, to);
    }
}
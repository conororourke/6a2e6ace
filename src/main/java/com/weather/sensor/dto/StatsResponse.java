package com.weather.sensor.dto;

import com.weather.sensor.model.Statistic;
import com.weather.sensor.model.WeatherMetric;

import java.time.LocalDate;
import java.util.List;

public record StatsResponse(LocalDate from, LocalDate to, List<StatResult> results) {

    public record StatResult(long sensorId, WeatherMetric metric, Statistic statistic, Double value) {
    }
}
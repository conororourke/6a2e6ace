package com.weather.sensor.model;

public record MetricAggregate(Double min, Double max, Double sum, Double avg) {

    public Double value(Statistic statistic) {
        return switch (statistic) {
            case MIN -> min;
            case MAX -> max;
            case SUM -> sum;
            case AVG -> avg;
        };
    }
}
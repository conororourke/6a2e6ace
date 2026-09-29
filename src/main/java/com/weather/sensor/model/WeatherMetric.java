package com.weather.sensor.model;

public enum WeatherMetric {
    TEMPERATURE("temperature"),
    HUMIDITY("humidity"),
    WIND_SPEED("wind_speed"),
    PRESSURE("pressure"),
    RAINFALL("rainfall");

    private final String column;

    WeatherMetric(String column) {
        this.column = column;
    }

    public String column() {
        return column;
    }
}

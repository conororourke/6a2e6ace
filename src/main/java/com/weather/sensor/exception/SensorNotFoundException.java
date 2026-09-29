package com.weather.sensor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.List;
import java.util.Set;

public class SensorNotFoundException extends ErrorResponseException {
    private SensorNotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail),
                null);
    }

    public static SensorNotFoundException byId(long id) {
        return new SensorNotFoundException("Sensor " + id + " not found");
    }

    public static SensorNotFoundException byIds(List<Long> ids) {
        return new SensorNotFoundException("Sensors " + ids + " not found");
    }

    public static SensorNotFoundException byName(String name) {
        return new SensorNotFoundException("Sensor '" + name + "' not found");
    }
}

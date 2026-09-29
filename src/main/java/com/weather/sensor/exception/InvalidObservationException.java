package com.weather.sensor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

public class InvalidObservationException extends ErrorResponseException {

    public InvalidObservationException(String field, String message) {
        super(HttpStatus.BAD_REQUEST, problem(field, message), null);
    }

    private static ProblemDetail problem(String field, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request.");
        problem.setProperty("errors", Map.of(field, message));
        return problem;
    }
}
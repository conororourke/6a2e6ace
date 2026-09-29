package com.weather.sensor.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class InvalidRequestException extends ErrorResponseException {

    public InvalidRequestException(String field, String message) {
        super(HttpStatus.BAD_REQUEST, problem(field, message), null);
    }

    private static ProblemDetail problem(String field, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problem.setProperty("errors", Map.of(field, message));
        return problem;
    }
}
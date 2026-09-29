package com.weather.sensor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchObservationRequest(
        @NotEmpty
        @Size(max = 1000)
        List<@Valid ObservationRequest> observations) {
}

package com.weather.sensor.controller;

import com.weather.sensor.dto.BatchObservationRequest;
import com.weather.sensor.dto.ObservationRequest;
import com.weather.sensor.service.ObservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensors/{sensorId}/observations")
public class ObservationController {

    private final ObservationService service;

    public ObservationController(ObservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> record(@PathVariable long sensorId, @Valid @RequestBody ObservationRequest request) {
        boolean stored = service.record(sensorId, request);
        return ResponseEntity.status(stored ? HttpStatus.CREATED : HttpStatus.OK).build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> recordBatch(@PathVariable long sensorId, @Valid @RequestBody BatchObservationRequest request) {
        boolean batchStored = service.recordBatch(sensorId, request.observations());
        return ResponseEntity.status(batchStored ? HttpStatus.CREATED : HttpStatus.OK).build();
    }
}
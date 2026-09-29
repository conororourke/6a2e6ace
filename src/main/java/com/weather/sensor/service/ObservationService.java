package com.weather.sensor.service;

import com.weather.sensor.dto.ObservationRequest;
import com.weather.sensor.exception.InvalidObservationException;
import com.weather.sensor.exception.SensorNotFoundException;
import com.weather.sensor.repository.ObservationRepository;
import com.weather.sensor.repository.SensorRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObservationService {

    private final SensorRepository sensorRepository;
    private final ObservationRepository observationRepository;
    private final Clock clock;

    public ObservationService(SensorRepository sensorRepository,
                              ObservationRepository observationRepository,
                              Clock clock) {
        this.sensorRepository = sensorRepository;
        this.observationRepository = observationRepository;
        this.clock = clock;
    }

    /** Returns true if stored, false if an observation already existed for that sensor and time. */
    public boolean record(long sensorId, ObservationRequest request) {
        if (!sensorRepository.existsById(sensorId)) {
            throw SensorNotFoundException.byId(sensorId);
        }

        Instant now = Instant.now(clock);
        if (request.recordedAt().isAfter(now)) {
            throw new InvalidObservationException("recordedAt", "must not be in the future");
        }

        return observationRepository.insert(request.toObservation(sensorId, now));
    }

    @Transactional
    public boolean recordBatch(long sensorId, List<ObservationRequest> requests) {
        boolean anyCreated = false;
        for (ObservationRequest request : requests) {
            if (record(sensorId, request)) {
                anyCreated = true;
            }
        }
        return anyCreated;
    }
}
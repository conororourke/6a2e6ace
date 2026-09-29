package com.weather.sensor.service;

import com.weather.sensor.dto.ObservationRequest;
import com.weather.sensor.exception.InvalidObservationException;
import com.weather.sensor.exception.SensorNotFoundException;
import com.weather.sensor.model.Observation;
import com.weather.sensor.repository.ObservationRepository;
import com.weather.sensor.repository.SensorRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

class ObservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-29T10:00:00Z");

    private final SensorRepository sensorRepoMock = mock(SensorRepository.class);
    private final ObservationRepository observationRepoMock = mock(ObservationRepository.class);
    private final ObservationService service = new ObservationService(
            sensorRepoMock, observationRepoMock, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void insertsObservationRecordedNow() {
        when(sensorRepoMock.existsById(1L)).thenReturn(true);

        service.record(1L, new ObservationRequest(NOW, 12.4, null, null, null, null));

        verify(observationRepoMock).insert(new Observation(1L, NOW, NOW, 12.4, null, null, null, null));
    }

    @Test
    void insertsObservationRecordedInThePast() {
        Instant recordedAt = NOW.minusSeconds(300);

        when(sensorRepoMock.existsById(1L)).thenReturn(true);

        service.record(1L, new ObservationRequest(recordedAt, 12.4, null, null, null, null));

        verify(observationRepoMock).insert(new Observation(1L, recordedAt, NOW, 12.4, null, null, null, null));
    }

    @Test
    void failsWhenRecordedAtInTheFuture() {
        Instant recordedAtPlus10Seconds = NOW.plusSeconds(10);
        ObservationRequest request =  new ObservationRequest(recordedAtPlus10Seconds, 12.4, null, null, null, null);

        when(sensorRepoMock.existsById(1L)).thenReturn(true);

        assertThrows(InvalidObservationException.class, () -> service.record(1L, request));
        verifyNoInteractions(observationRepoMock);
    }

    @Test
    void failsWhenSensorNotFound() {
        when(sensorRepoMock.existsById(1L)).thenReturn(false);

        assertThrows(SensorNotFoundException.class, () -> service.record(2L, new ObservationRequest(NOW, 12.4, null, null, null, null)));
        verifyNoInteractions(observationRepoMock);
    }


}
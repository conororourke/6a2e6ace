package com.weather.sensor.service;

import com.weather.sensor.dto.SensorResponse;
import com.weather.sensor.exception.SensorNotFoundException;
import com.weather.sensor.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorService {

    private final SensorRepository repository;

    public SensorService(SensorRepository repository) {
        this.repository = repository;
    }

    public List<SensorResponse> findAll() {
        return repository.findAllByOrderByIdAsc().stream()
                    .map(SensorResponse::from)
                    .toList();
    }

    public SensorResponse getById(long id) {
        return repository.findById(id)
                .map(SensorResponse::from)
                .orElseThrow(() -> SensorNotFoundException.byId(id));
    }

    public SensorResponse getByName(String name) {
        return repository.findByNameIgnoreCase(name.trim())
                .map(SensorResponse::from)
                .orElseThrow(() -> SensorNotFoundException.byName(name));
    }

}
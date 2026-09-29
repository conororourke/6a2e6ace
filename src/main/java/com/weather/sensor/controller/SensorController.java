package com.weather.sensor.controller;

import com.weather.sensor.dto.SensorResponse;
import com.weather.sensor.service.SensorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorController {

    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    @GetMapping
    public List<SensorResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SensorResponse getById(@PathVariable long id) {
        return service.getById(id);
    }

    @GetMapping("/by-name/{name}")
    public SensorResponse getByName(@PathVariable String name) {
        return service.getByName(name);
    }
}
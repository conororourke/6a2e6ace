package com.weather.sensor.repository;

import com.weather.sensor.model.Sensor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends ListCrudRepository<Sensor, Long> {
    List<Sensor> findAllByOrderByIdAsc();

    Optional<Sensor> findByNameIgnoreCase(String name);

    @Query("SELECT id FROM sensor ORDER BY id")
    List<Long> findAllIds();

}
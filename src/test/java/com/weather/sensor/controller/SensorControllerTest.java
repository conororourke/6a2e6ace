package com.weather.sensor.controller;

import com.weather.sensor.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SensorControllerTest {

    private static final String SENSORS = "/api/v1/sensors";

    @Autowired
    MockMvc mockMvc;

    // findAll
    @Test
    void findAllReturnsSeededSensorsInIdOrder() throws Exception {
        mockMvc.perform(get(SENSORS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].name").value("Athenry"))
                .andExpect(jsonPath("$[5].name").value("Phoenix Park"));
    }

    // getById
    @Test
    void getByIdReturnsSensor() throws Exception {
        mockMvc.perform(get(SENSORS + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Athenry"))
                .andExpect(jsonPath("$.latitude").value(53.289167))
                .andExpect(jsonPath("$.longitude").value(-8.785556));
    }

    @Test
    void getByIdReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get(SENSORS + "/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Sensor 999999 not found"));
    }

    @Test
    void getByIdReturns400ForNonNumericId() throws Exception {
        mockMvc.perform(get(SENSORS + "/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // getByName
    @Test
    void getByNameReturnsSensor() throws Exception {
        mockMvc.perform(get(SENSORS + "/by-name/{name}", "Belmullet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Belmullet"));
    }

    @Test
    void getByNameIgnoresCase() throws Exception {
        mockMvc.perform(get(SENSORS + "/by-name/{name}", "malin head"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Malin Head"));
    }

    @Test
    void getByNameReturns404ForUnknownName() throws Exception {
        mockMvc.perform(get(SENSORS + "/by-name/{name}", "Atlantis"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Sensor 'Atlantis' not found"));
    }
}
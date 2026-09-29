package com.weather.sensor.controller;

import com.weather.sensor.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ObservationControllerTest {

    private static final String URL = "/api/v1/sensors/{sensorId}/observations";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbc;

    @Test
    void storesNewObservation() throws Exception {
        post(1, """
                {"recordedAt": "2026-09-01T10:00:00Z", "temperature": 12.4, "humidity": 81}
                """)
                .andExpect(status().isCreated());

        Map<String, Object> row = findObservation(1, "2026-09-01T10:00:00Z");
        assertEquals(12.4, row.get("temperature"));
        assertEquals(81.0, row.get("humidity"));
        assertNull(row.get("wind_speed"));
    }

    @Test
    void returns404ForUnknownSensor() throws Exception {
        post(999999, """
                {"recordedAt": "2026-09-01T10:00:00Z", "temperature": 12.4}
                """)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Sensor 999999 not found"));
    }

    @Test
    void rejectsObservationWithNoMetrics() throws Exception {
        post(1, """
                {"recordedAt": "2026-09-01T10:00:00Z"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.atLeastOneObservationMetricPresent")
                        .value("at least one observation metric must exist"));
    }

    @Test
    void rejectsNegativeWindSpeed() throws Exception {
        post(1, """
                {"recordedAt": "2026-09-01T10:00:00Z", "windSpeed": -1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.windSpeed").exists());
    }

    @Test
    void batchSavesMultipleRecords() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/batch", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {"observations": [
                  {"recordedAt": "2026-09-02T10:01:00Z", "temperature": 12.5, "humidity": 10.0, "windSpeed": 20.0, "pressure": 1000.0, "rainfall": 5.0},
                  {"recordedAt": "2026-09-02T10:02:00Z", "temperature": 12.6, "humidity": 11.0, "windSpeed": 21.0, "pressure": 1001.0, "rainfall": 5.1}
                ]}
                """)).andExpect(status().isCreated());

        assertEquals(1, countObservations(1, "2026-09-02T10:01:00Z"));
        assertEquals(1, countObservations(1, "2026-09-02T10:02:00Z"));
        assertEquals(12.5, findObservation(1, "2026-09-02T10:01:00Z").get("temperature"));
    }

    private ResultActions post(long sensorId, String body) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(URL, sensorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private Map<String, Object> findObservation(long sensorId, String recordedAt) {
        return jdbc.sql("""
                        SELECT temperature, humidity, wind_speed FROM observation
                        WHERE sensor_id = :sensorId AND recorded_at = CAST(:recordedAt AS TIMESTAMPTZ)
                        """)
                .param("sensorId", sensorId)
                .param("recordedAt", recordedAt)
                .query().singleRow();
    }

    private int countObservations(long sensorId, String recordedAt) {
        return jdbc.sql("""
                    SELECT COUNT(*) FROM observation
                    WHERE sensor_id = :sensorId AND recorded_at = CAST(:recordedAt AS TIMESTAMPTZ)
                    """)
                .param("sensorId", sensorId)
                .param("recordedAt", recordedAt)
                .query(Integer.class)
                .single();
    }

}
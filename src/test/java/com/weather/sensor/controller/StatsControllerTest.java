package com.weather.sensor.controller;

import com.weather.sensor.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class StatsControllerTest {

    private static final String URL = "/api/v1/stats";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbc;

    @Test
    void averagesEveryMetric() throws Exception {
        insert(1, "2026-09-18T09:00:00Z", 10.0, 80.0, 5.0, 1000.0, 0.5);
        insert(2, "2026-09-18T09:00:00Z", 8.0, 80.0, 1.0, 1000.0, 2.5);

        insert(1, "2026-09-20T10:00:00Z", 20.0, 90.0, 15.0, 1010.0, 1.0);
        insert(2, "2026-09-20T10:00:00Z", 16.0, 90.0, 1.0, 1010.0, 2.0);

        mockMvc.perform(get(URL)
                        .param("weatherMetrics", "TEMPERATURE,HUMIDITY,WIND_SPEED,PRESSURE,RAINFALL")
                        .param("statistics", "AVG,MAX")
                        .param("sensorIds", "1,2")
                        .param("from", "2026-09-18")
                        .param("to", "2026-09-20"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                      "from": "2026-09-18",
                      "to": "2026-09-20",
                      "results": [
                        {"sensorId": 1, "metric": "TEMPERATURE", "statistic": "AVG", "value": 15.0},
                        {"sensorId": 1, "metric": "TEMPERATURE", "statistic": "MAX", "value": 20.0},
                        {"sensorId": 1, "metric": "HUMIDITY",    "statistic": "AVG", "value": 85.0},
                        {"sensorId": 1, "metric": "HUMIDITY",    "statistic": "MAX", "value": 90.0},
                        {"sensorId": 1, "metric": "WIND_SPEED",  "statistic": "AVG", "value": 10.0},
                        {"sensorId": 1, "metric": "WIND_SPEED",  "statistic": "MAX", "value": 15.0},
                        {"sensorId": 1, "metric": "PRESSURE",    "statistic": "AVG", "value": 1005.0},
                        {"sensorId": 1, "metric": "PRESSURE",    "statistic": "MAX", "value": 1010.0},
                        {"sensorId": 1, "metric": "RAINFALL",    "statistic": "AVG", "value": 0.75},
                        {"sensorId": 1, "metric": "RAINFALL",    "statistic": "MAX", "value": 1.0},
                        {"sensorId": 2, "metric": "TEMPERATURE", "statistic": "AVG", "value": 12.0},
                        {"sensorId": 2, "metric": "TEMPERATURE", "statistic": "MAX", "value": 16.0},
                        {"sensorId": 2, "metric": "HUMIDITY",    "statistic": "AVG", "value": 85.0},
                        {"sensorId": 2, "metric": "HUMIDITY",    "statistic": "MAX", "value": 90.0},
                        {"sensorId": 2, "metric": "WIND_SPEED",  "statistic": "AVG", "value": 1.0},
                        {"sensorId": 2, "metric": "WIND_SPEED",  "statistic": "MAX", "value": 1.0},
                        {"sensorId": 2, "metric": "PRESSURE",    "statistic": "AVG", "value": 1005.0},
                        {"sensorId": 2, "metric": "PRESSURE",    "statistic": "MAX", "value": 1010.0},
                        {"sensorId": 2, "metric": "RAINFALL",    "statistic": "AVG", "value": 2.25},
                        {"sensorId": 2, "metric": "RAINFALL",    "statistic": "MAX", "value": 2.5}
                      ]
                    }
                    """, JsonCompareMode.STRICT));
    }

    private void insert(long sensorId, String recordedAt, Double temperature, Double humidity,
                        Double windSpeed, Double pressure, Double rainfall) {
        jdbc.sql("""
                        INSERT INTO observation
                            (sensor_id, recorded_at, temperature, humidity, wind_speed, pressure, rainfall)
                        VALUES
                            (:sensorId, CAST(:recordedAt AS TIMESTAMPTZ), :temperature, :humidity,
                             :windSpeed, :pressure, :rainfall)
                        """)
                .param("sensorId", sensorId)
                .param("recordedAt", recordedAt)
                .param("temperature", temperature)
                .param("humidity", humidity)
                .param("windSpeed", windSpeed)
                .param("pressure", pressure)
                .param("rainfall", rainfall)
                .update();
    }
}
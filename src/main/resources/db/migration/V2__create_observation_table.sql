CREATE TABLE observation (
  sensor_id    BIGINT      NOT NULL REFERENCES sensor (id) ON DELETE RESTRICT,
  recorded_at  TIMESTAMPTZ NOT NULL,
  received_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  temperature  DOUBLE PRECISION,
  humidity     DOUBLE PRECISION CHECK (humidity BETWEEN 0 AND 100),
  wind_speed   DOUBLE PRECISION CHECK (wind_speed >= 0),
  pressure     DOUBLE PRECISION CHECK (pressure > 0),
  rainfall     DOUBLE PRECISION CHECK (rainfall >= 0),
  PRIMARY KEY (sensor_id, recorded_at),
  CHECK (num_nonnulls(temperature, humidity, wind_speed, pressure, rainfall) > 0)
);
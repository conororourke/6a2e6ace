# Weather Sensor API

A REST API that receives weather observations (temperature, humidity, wind speed, pressure and rainfall) from sensors.
Clients can query for statistical metrics (avg, min, max, sum) based on the sensor and weather observation type over a date range.

**Tech Stack:** Java 21, Spring Boot 4, PostgreSQL 17, Docker
 
---

## Design decisions
The brief outlined how the REST API should behave but gave very little detail to inform design decisions.
I decided to look at how Met Eireann records sensor data: https://www.met.ie/climate/what-we-measure
*"There are 20 fully automatic weather stations across the country recording meteorological elements on a minute-by-minute basis. These data are transferred back at hourly intervals..."*

- All weather metrics are recorded once per minute (allows for null values in case one of the readings is not being recorded by the weather sensor).
- Sensors are added with a migration script (only a sample of 6 inserted)
- POST endpoints are available for both single and batched observations
- Statistics are based per sensor because weather reports are typically local. I.e. we do not average over all sensors
- SUM is only allowed for rainfall (e.g. sum of temperature doesn't make sense).
- GET stats request without a date range uses today's date.


## Out of scope

- Authentication and authorisation
- Environment config 


## Potential next steps

- Ability to correct history
- The POST batch is currently doing one insert per observation. It should be doing a batched insert.
- Depending on how the application will be queried we should look at potential performance improvements (e.g. caching stats result)
- Review which weather metrics vs statistics make sense. For now only SUM can only be used for rainfall (requesting SUM TEMPERATURE will be rejected).


## How to run the application

```bash
docker compose --profile app up --build
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Tests: `./mvnw verify`


## Example requests

```bash
 
# Sensors
curl -s "http://localhost:8080/api/v1/sensors"
curl -s "http://localhost:8080/api/v1/sensors/by-name/athenry"
 
# Record a single observation
curl -i -X POST "http://localhost:8080/api/v1/sensors/1/observations" \
  -H "Content-Type: application/json" \
  -d '{"recordedAt": "2026-09-01T10:00:00Z", "temperature": 12.4, "humidity": 81, "windSpeed": 6.2, "pressure": 1013.2, "rainfall": 0.4}'

# Record a batch of observations
curl -i -X POST "http://localhost:8080/api/v1/sensors/1/observations/batch" \
  -H "Content-Type: application/json" \
  -d '{"observations": [
        {"recordedAt": "2026-09-01T09:00:00Z", "temperature": 11.8, "humidity": 84, "windSpeed": 5.1, "pressure": 1012.8, "rainfall": 0.2},
        {"recordedAt": "2026-09-01T09:01:00Z", "temperature": 11.9, "humidity": 83, "windSpeed": 5.4, "pressure": 1012.7, "rainfall": 0.0},
        {"recordedAt": "2026-09-01T09:02:00Z", "temperature": 12.0, "humidity": 83, "windSpeed": 6.0, "pressure": 1012.7, "rainfall": 0.0},
        {"recordedAt": "2026-09-01T09:03:00Z", "temperature": 12.1, "humidity": 82, "windSpeed": 5.8, "pressure": 1012.6, "rainfall": 0.1},
        {"recordedAt": "2026-09-01T09:04:00Z", "temperature": 12.1, "humidity": 82, "windSpeed": 6.3, "pressure": 1012.6, "rainfall": 0.3},
        {"recordedAt": "2026-09-01T09:05:00Z", "temperature": 12.2, "humidity": 81, "windSpeed": 7.1, "pressure": 1012.5, "rainfall": 0.4},
        {"recordedAt": "2026-09-01T09:06:00Z", "temperature": 12.3, "humidity": 81, "windSpeed": 6.8, "pressure": 1012.5, "rainfall": 0.2},
        {"recordedAt": "2026-09-01T09:07:00Z", "temperature": 12.4, "humidity": 80, "windSpeed": 6.5, "pressure": 1012.4, "rainfall": 0.0},
        {"recordedAt": "2026-09-01T09:08:00Z", "temperature": 12.5, "humidity": 80, "windSpeed": 5.9, "pressure": 1012.4, "rainfall": 0.0},
        {"recordedAt": "2026-09-01T09:09:00Z", "temperature": 12.6, "humidity": 79, "windSpeed": 5.6, "pressure": 1012.3, "rainfall": 0.1}
      ]}'
 
# Average temperature and humidity for sensor 1 over a week
curl -s "http://localhost:8080/api/v1/stats?weatherMetrics=TEMPERATURE,HUMIDITY&sensorIds=1&from=2026-09-01&to=2026-09-07"
 
# Min and max temperature for sensors 1 and 2
curl -s "http://localhost:8080/api/v1/stats?weatherMetrics=TEMPERATURE&statistics=MIN,MAX&sensorIds=1,2&from=2026-09-01&to=2026-09-07"
 
# Total rainfall for one day
curl -s "http://localhost:8080/api/v1/stats?weatherMetrics=RAINFALL&statistics=SUM&sensorIds=1&from=2026-09-01&to=2026-09-01"
 
# Today so far, all sensors
curl -s "http://localhost:8080/api/v1/stats?weatherMetrics=TEMPERATURE"

# Example errors
curl -i "http://localhost:8080/api/v1/stats?weatherMetrics=TEMPERATURE&sensorIds=1,999"                 # 404 unknown sensor
curl -i "http://localhost:8080/api/v1/stats?weatherMetrics=TEMPERATURE&from=2026-08-01&to=2026-09-07"   # 400 range over 31 days
```

## View POSTGRES tables

```
docker compose exec postgres psql -U weather -d weather

SELECT * FROM sensor ORDER BY id;

SELECT * FROM observation ORDER BY sensor_id, recorded_at DESC LIMIT 20;
```
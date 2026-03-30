package adapter.outbound.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenMeteoForecastResponse {

    private Current current;
    private Daily daily;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Current {
        @JsonProperty("temperature_2m")
        private Double temperature2m;

        @JsonProperty("apparent_temperature")
        private Double apparentTemperature;

        @JsonProperty("relative_humidity_2m")
        private Integer relativeHumidity2m;

        @JsonProperty("wind_speed_10m")
        private Double windSpeed10m;

        private Double visibility;

        @JsonProperty("surface_pressure")
        private Double surfacePressure;

        @JsonProperty("weather_code")
        private Integer weatherCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Daily {
        private List<String> time;

        @JsonProperty("temperature_2m_max")
        private List<Double> temperature2mMax;

        @JsonProperty("temperature_2m_min")
        private List<Double> temperature2mMin;

        @JsonProperty("weather_code")
        private List<Integer> weatherCode;
    }
}

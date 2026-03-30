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
public class WeatherApiForecastResponse {

    private Location location;
    private Current current;
    private Forecast forecast;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String name;
        private String region;
        private String country;
        private Double lat;
        private Double lon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Current {
        @JsonProperty("temp_c")
        private Double tempC;

        @JsonProperty("feelslike_c")
        private Double feelsLikeC;

        private Integer humidity;

        @JsonProperty("wind_kph")
        private Double windKph;

        @JsonProperty("vis_km")
        private Double visibilityKm;

        @JsonProperty("pressure_mb")
        private Double pressureMb;

        private Condition condition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Forecast {
        @JsonProperty("forecastday")
        private List<ForecastDay> forecastday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDay {
        private String date;
        private Day day;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Day {
        @JsonProperty("maxtemp_c")
        private Double maxTempC;

        @JsonProperty("mintemp_c")
        private Double minTempC;

        private Condition condition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Condition {
        private String text;
    }
}

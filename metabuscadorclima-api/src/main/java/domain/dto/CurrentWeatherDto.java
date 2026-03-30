package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherDto {

    private Double temperatureC;
    private Double feelsLikeC;
    private Integer humidity;
    private Double windKph;
    private Double visibilityKm;
    private Double pressureHpa;
    private String condition;
    private Double maxTempC;
    private Double minTempC;
}

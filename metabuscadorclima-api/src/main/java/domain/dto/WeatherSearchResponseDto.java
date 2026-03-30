package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSearchResponseDto {

    private String query;
    private LocationDto location;
    private List<SourceWeatherDto> sources = new ArrayList<>();
    private ConsolidatedWeatherDto consolidated;
    private List<ForecastDayDto> forecast = new ArrayList<>();
}

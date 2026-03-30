package application.usecase.weather.search;

import domain.dto.ConsolidatedWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.SourceWeatherDto;
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
public class SearchWeatherOutput {

    private String query;
    private LocationDto location;
    private List<SourceWeatherDto> sources = new ArrayList<>();
    private ConsolidatedWeatherDto consolidated;
    private List<ForecastDayDto> forecast = new ArrayList<>();
}

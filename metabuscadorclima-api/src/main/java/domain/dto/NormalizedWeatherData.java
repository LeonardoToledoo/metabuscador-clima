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
public class NormalizedWeatherData {

    private String source;
    private LocationDto location;
    private CurrentWeatherDto current;
    private List<ForecastDayDto> forecast = new ArrayList<>();
}

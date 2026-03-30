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
public class SourceWeatherDto {

    private String source;
    private boolean available;
    private String message;
    private CurrentWeatherDto current;
    private List<ForecastDayDto> forecast = new ArrayList<>();
}

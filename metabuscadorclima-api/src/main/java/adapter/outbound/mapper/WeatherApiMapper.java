package adapter.outbound.mapper;

import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import adapter.outbound.client.WeatherApiForecastResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class WeatherApiMapper {

    public NormalizedWeatherData toNormalized(WeatherApiForecastResponse response) {
        if (Objects.isNull(response) || Objects.isNull(response.getCurrent()) || Objects.isNull(response.getLocation())) {
            throw new IllegalArgumentException("Resposta da WeatherAPI incompleta.");
        }

        List<ForecastDayDto> forecast = mapForecast(response);
        ForecastDayDto today = forecast.isEmpty() ? null : forecast.get(0);

        LocationDto location = LocationDto.builder()
                .name(response.getLocation().getName())
                .region(response.getLocation().getRegion())
                .country(response.getLocation().getCountry())
                .latitude(response.getLocation().getLat())
                .longitude(response.getLocation().getLon())
                .build();

        CurrentWeatherDto current = CurrentWeatherDto.builder()
                .temperatureC(response.getCurrent().getTempC())
                .feelsLikeC(response.getCurrent().getFeelsLikeC())
                .humidity(response.getCurrent().getHumidity())
                .windKph(response.getCurrent().getWindKph())
                .visibilityKm(response.getCurrent().getVisibilityKm())
                .pressureHpa(response.getCurrent().getPressureMb())
                .condition(Objects.nonNull(response.getCurrent().getCondition()) ? response.getCurrent().getCondition().getText() : "Condição não informada")
                .maxTempC(Objects.isNull(today) ? null : today.getMaxTempC())
                .minTempC(Objects.isNull(today) ? null : today.getMinTempC())
                .build();

        return NormalizedWeatherData.builder()
                .source("WeatherAPI")
                .location(location)
                .current(current)
                .forecast(forecast)
                .build();
    }

    private List<ForecastDayDto> mapForecast(WeatherApiForecastResponse response) {
        if (Objects.isNull(response.getForecast()) || Objects.isNull(response.getForecast().getForecastday())) {
            return Collections.emptyList();
        }

        List<ForecastDayDto> result = new ArrayList<>();
        for (WeatherApiForecastResponse.ForecastDay day : response.getForecast().getForecastday()) {
            String condition = Objects.isNull(day.getDay()) || Objects.isNull(day.getDay().getCondition())
                    ? "Condição não informada"
                    : day.getDay().getCondition().getText();

            result.add(ForecastDayDto.builder()
                    .date(day.getDate())
                    .minTempC(Objects.isNull(day.getDay()) ? null : day.getDay().getMinTempC())
                    .maxTempC(Objects.isNull(day.getDay()) ? null : day.getDay().getMaxTempC())
                    .condition(condition)
                    .build());
        }
        return result;
    }
}

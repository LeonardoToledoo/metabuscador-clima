package adapter.outbound.mapper;

import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import adapter.outbound.client.OpenMeteoForecastResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OpenMeteoMapper {

    private static final Map<Integer, String> WEATHER_CODE_MAP = Map.ofEntries(
            Map.entry(0, "Céu limpo"),
            Map.entry(1, "Principalmente limpo"),
            Map.entry(2, "Parcialmente nublado"),
            Map.entry(3, "Nublado"),
            Map.entry(45, "Névoa"),
            Map.entry(48, "Névoa com geada"),
            Map.entry(51, "Garoa leve"),
            Map.entry(53, "Garoa moderada"),
            Map.entry(55, "Garoa forte"),
            Map.entry(61, "Chuva leve"),
            Map.entry(63, "Chuva moderada"),
            Map.entry(65, "Chuva forte"),
            Map.entry(71, "Neve leve"),
            Map.entry(80, "Pancadas de chuva"),
            Map.entry(95, "Tempestade")
    );

    public NormalizedWeatherData toNormalized(OpenMeteoForecastResponse response, LocationDto location) {
        if (Objects.isNull(response) || Objects.isNull(response.getCurrent())) {
            throw new IllegalArgumentException("Resposta da Open-Meteo sem dados de clima atual.");
        }

        List<ForecastDayDto> forecast = mapForecast(response.getDaily());
        ForecastDayDto today = forecast.isEmpty() ? null : forecast.get(0);

        CurrentWeatherDto current = CurrentWeatherDto.builder()
                .temperatureC(response.getCurrent().getTemperature2m())
                .feelsLikeC(response.getCurrent().getApparentTemperature())
                .humidity(response.getCurrent().getRelativeHumidity2m())
                .windKph(response.getCurrent().getWindSpeed10m())
                .visibilityKm(toKilometers(response.getCurrent().getVisibility()))
                .pressureHpa(response.getCurrent().getSurfacePressure())
                .condition(weatherCodeToText(response.getCurrent().getWeatherCode()))
                .maxTempC(Objects.nonNull(today) ? today.getMaxTempC() : null)
                .minTempC(Objects.nonNull(today) ? today.getMinTempC() : null)
                .build();

        return NormalizedWeatherData.builder()
                .source("Open-Meteo")
                .location(location)
                .current(current)
                .forecast(forecast)
                .build();
    }

    private List<ForecastDayDto> mapForecast(OpenMeteoForecastResponse.Daily daily) {
        if (Objects.isNull(daily) || Objects.isNull(daily.getTime())) {
            return Collections.emptyList();
        }

        List<ForecastDayDto> result = new ArrayList<>();
        int size = daily.getTime().size();

        for (int i = 0; i < size; i++) {
            String date = daily.getTime().get(i);
            Double max = safeGetDouble(daily.getTemperature2mMax(), i);
            Double min = safeGetDouble(daily.getTemperature2mMin(), i);
            Integer code = safeGetInteger(daily.getWeatherCode(), i);
            result.add(ForecastDayDto.builder()
                    .date(date)
                    .minTempC(min)
                    .maxTempC(max)
                    .condition(weatherCodeToText(code))
                    .build());
        }
        return result;
    }

    private Double safeGetDouble(List<Double> list, int index) {
        return (Objects.nonNull(list) && index < list.size()) ? list.get(index) : null;
    }

    private Integer safeGetInteger(List<Integer> list, int index) {
        return (Objects.nonNull(list) && index < list.size()) ? list.get(index) : null;
    }

    private Double toKilometers(Double visibilityInMeters) {
        if (Objects.isNull(visibilityInMeters)) {
            return null;
        }
        return visibilityInMeters / 1000.0;
    }

    private String weatherCodeToText(Integer code) {
        return WEATHER_CODE_MAP.getOrDefault(code, "Condição não informada");
    }
}

package service;

import application.config.WeatherApiProperties;
import application.service.WeatherAggregatorService;
import domain.dto.ConsolidatedWeatherDto;
import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import adapter.outbound.client.OpenMeteoClient;
import adapter.outbound.client.WeatherApiClient;
import adapter.outbound.mapper.OpenMeteoMapper;
import adapter.outbound.mapper.WeatherApiMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherAggregatorServiceTest {

    @Test
    void shouldConsolidateUsingAverageWhenTwoSourcesAvailable() {
        WeatherAggregatorService service = new WeatherAggregatorService(
                (OpenMeteoClient) null,
                (WeatherApiClient) null,
                new OpenMeteoMapper(),
                new WeatherApiMapper(),
                new WeatherApiProperties()
        );

        NormalizedWeatherData source1 = new NormalizedWeatherData(
                "Open-Meteo",
                new LocationDto("Campo Grande", "MS", "Brasil", -20.45, -54.62),
                new CurrentWeatherDto(28.0, 29.0, 60, 12.0, 10.0, 1008.0, "Parcialmente nublado", 32.0, 22.0),
                List.of(new ForecastDayDto("2026-03-29", 22.0, 32.0, "Parcialmente nublado"))
        );

        NormalizedWeatherData source2 = new NormalizedWeatherData(
                "WeatherAPI",
                new LocationDto("Campo Grande", "MS", "Brasil", -20.45, -54.62),
                new CurrentWeatherDto(26.0, 28.0, 70, 8.0, 14.0, 1012.0, "Ensolarado", 30.0, 20.0),
                List.of(new ForecastDayDto("2026-03-29", 20.0, 30.0, "Ensolarado"))
        );

        ConsolidatedWeatherDto consolidated = service.consolidateCurrent(List.of(source1, source2));

        assertEquals(27.0, consolidated.getCurrent().getTemperatureC());
        assertEquals(28.5, consolidated.getCurrent().getFeelsLikeC());
        assertEquals(65, consolidated.getCurrent().getHumidity());
        assertEquals(10.0, consolidated.getCurrent().getWindKph());
        assertEquals(12.0, consolidated.getCurrent().getVisibilityKm());
        assertEquals(1010.0, consolidated.getCurrent().getPressureHpa());
        assertEquals(31.0, consolidated.getCurrent().getMaxTempC());
        assertEquals(21.0, consolidated.getCurrent().getMinTempC());
        assertEquals(2, consolidated.getSourcesUsed().size());
    }
}

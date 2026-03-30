package mapper;

import adapter.outbound.mapper.OpenMeteoMapper;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import adapter.outbound.client.OpenMeteoForecastResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenMeteoMapperTest {

    private final OpenMeteoMapper mapper = new OpenMeteoMapper();

    @Test
    void shouldNormalizeOpenMeteoResponse() {
        OpenMeteoForecastResponse response = new OpenMeteoForecastResponse();

        OpenMeteoForecastResponse.Current current = new OpenMeteoForecastResponse.Current();
        current.setTemperature2m(28.0);
        current.setApparentTemperature(30.0);
        current.setRelativeHumidity2m(65);
        current.setWindSpeed10m(12.0);
        current.setVisibility(10000.0);
        current.setSurfacePressure(1009.0);
        current.setWeatherCode(1);
        response.setCurrent(current);

        OpenMeteoForecastResponse.Daily daily = new OpenMeteoForecastResponse.Daily();
        daily.setTime(List.of("2026-03-29", "2026-03-30"));
        daily.setTemperature2mMax(List.of(32.0, 31.0));
        daily.setTemperature2mMin(List.of(22.0, 21.0));
        daily.setWeatherCode(List.of(1, 3));
        response.setDaily(daily);

        LocationDto location = new LocationDto("Campo Grande", "MS", "Brasil", -20.45, -54.62);

        NormalizedWeatherData normalized = mapper.toNormalized(response, location);

        assertEquals("Open-Meteo", normalized.getSource());
        assertEquals(28.0, normalized.getCurrent().getTemperatureC());
        assertEquals(30.0, normalized.getCurrent().getFeelsLikeC());
        assertEquals(65, normalized.getCurrent().getHumidity());
        assertEquals(10.0, normalized.getCurrent().getVisibilityKm());
        assertEquals(1009.0, normalized.getCurrent().getPressureHpa());
        assertEquals("Principalmente limpo", normalized.getCurrent().getCondition());
        assertEquals(2, normalized.getForecast().size());
        assertEquals("2026-03-29", normalized.getForecast().get(0).getDate());
    }
}

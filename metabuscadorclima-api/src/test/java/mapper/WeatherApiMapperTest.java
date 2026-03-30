package mapper;

import adapter.outbound.mapper.WeatherApiMapper;
import domain.dto.NormalizedWeatherData;
import adapter.outbound.client.WeatherApiForecastResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherApiMapperTest {

    private final WeatherApiMapper mapper = new WeatherApiMapper();

    @Test
    void shouldNormalizeWeatherApiResponse() {
        WeatherApiForecastResponse response = new WeatherApiForecastResponse();

        WeatherApiForecastResponse.Location location = new WeatherApiForecastResponse.Location();
        location.setName("Campo Grande");
        location.setRegion("Mato Grosso do Sul");
        location.setCountry("Brasil");
        location.setLat(-20.45);
        location.setLon(-54.62);
        response.setLocation(location);

        WeatherApiForecastResponse.Current current = new WeatherApiForecastResponse.Current();
        current.setTempC(27.5);
        current.setFeelsLikeC(29.0);
        current.setHumidity(70);
        current.setWindKph(10.5);
        current.setVisibilityKm(15.0);
        current.setPressureMb(1012.0);

        WeatherApiForecastResponse.Condition condition = new WeatherApiForecastResponse.Condition();
        condition.setText("Ensolarado");
        current.setCondition(condition);
        response.setCurrent(current);

        WeatherApiForecastResponse.Day day = new WeatherApiForecastResponse.Day();
        day.setMinTempC(21.0);
        day.setMaxTempC(31.0);
        day.setCondition(condition);

        WeatherApiForecastResponse.ForecastDay forecastDay = new WeatherApiForecastResponse.ForecastDay();
        forecastDay.setDate("2026-03-29");
        forecastDay.setDay(day);

        WeatherApiForecastResponse.Forecast forecast = new WeatherApiForecastResponse.Forecast();
        forecast.setForecastday(List.of(forecastDay));
        response.setForecast(forecast);

        NormalizedWeatherData normalized = mapper.toNormalized(response);

        assertEquals("WeatherAPI", normalized.getSource());
        assertEquals("Campo Grande", normalized.getLocation().getName());
        assertEquals(27.5, normalized.getCurrent().getTemperatureC());
        assertEquals("Ensolarado", normalized.getCurrent().getCondition());
        assertEquals(15.0, normalized.getCurrent().getVisibilityKm());
        assertEquals(1012.0, normalized.getCurrent().getPressureHpa());
        assertEquals(1, normalized.getForecast().size());
        assertEquals(31.0, normalized.getForecast().get(0).getMaxTempC());
    }
}

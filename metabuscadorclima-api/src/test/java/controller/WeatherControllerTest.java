package controller;

import adapter.entrypoint.controller.weather.search.SearchWeatherController;
import application.usecase.weather.search.SearchWeatherInput;
import application.usecase.weather.search.SearchWeatherOutput;
import application.usecase.weather.search.SearchWeatherUseCase;
import domain.dto.ConsolidatedWeatherDto;
import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.SourceWeatherDto;
import domain.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeatherControllerTest {

    @Test
    void shouldReturnWeatherSearchResponse() throws Exception {
        SearchWeatherOutput payload = new SearchWeatherOutput(
                "Campo Grande",
                new LocationDto("Campo Grande", "MS", "Brasil", -20.45, -54.62),
                List.of(new SourceWeatherDto(
                        "Open-Meteo",
                        true,
                        "Dados carregados com sucesso.",
                        new CurrentWeatherDto(27.0, 29.0, 65, 10.0, 12.0, 1011.0, "Parcialmente nublado", 31.0, 21.0),
                        List.of(new ForecastDayDto("2026-03-29", 21.0, 31.0, "Parcialmente nublado"))
                )),
                new ConsolidatedWeatherDto(
                        List.of("Open-Meteo"),
                        new CurrentWeatherDto(27.0, 29.0, 65, 10.0, 12.0, 1011.0, "Parcialmente nublado", 31.0, 21.0)
                ),
                List.of(new ForecastDayDto("2026-03-29", 21.0, 31.0, "Parcialmente nublado"))
        );

        SearchWeatherUseCase fakeUseCase = new SearchWeatherUseCase() {
            @Override
            public SearchWeatherOutput executar(SearchWeatherInput inputData) {
                return payload;
            }
        };

        SearchWeatherController controller = new SearchWeatherController(fakeUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/weather/search")
                        .param("city", "Campo Grande")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Campo Grande"))
                .andExpect(jsonPath("$.location.name").value("Campo Grande"))
                .andExpect(jsonPath("$.consolidated.current.temperatureC").value(27.0));
    }
}

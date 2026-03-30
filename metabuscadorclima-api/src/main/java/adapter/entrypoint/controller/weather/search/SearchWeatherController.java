package adapter.entrypoint.controller.weather.search;

import application.usecase.weather.search.SearchWeatherInput;
import application.usecase.weather.search.SearchWeatherOutput;
import application.usecase.weather.search.SearchWeatherUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Endpoint de busca consolidada de clima")
public class SearchWeatherController {

    private final SearchWeatherUseCase useCase;

    @GetMapping("/search")
    @Operation(
            summary = "Buscar clima por cidade",
            description = "Consulta Open-Meteo e WeatherAPI, normaliza e consolida os dados.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Consulta concluída"),
                    @ApiResponse(responseCode = "404", description = "Cidade não encontrada"),
                    @ApiResponse(responseCode = "502", description = "Falha em serviços externos")
            }
    )
    public SearchWeatherOutput search(
            @Parameter(description = "Nome da cidade para busca", example = "Campo Grande")
            @RequestParam @NotBlank String city) {
        SearchWeatherInput inputData = SearchWeatherInput.builder()
                .city(city)
                .build();
        return useCase.executar(inputData);
    }
}

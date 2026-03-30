package adapter.entrypoint.controller.location.search;

import application.usecase.location.search.LocationSearchEnum;
import application.usecase.location.search.SearchLocationInput;
import application.usecase.location.search.SearchLocationOutput;
import application.usecase.location.search.SearchLocationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Autocomplete de países, estados e cidades")
public class SearchLocationController {

    private final SearchLocationUseCase useCase;

    @GetMapping("/countries")
    @Operation(summary = "Buscar países por prefixo")
    public SearchLocationOutput searchCountries(
            @Parameter(description = "Texto para filtro por prefixo", example = "Bra")
            @RequestParam(required = false) String q) {
        return useCase.executar(SearchLocationInput.builder()
                .type(LocationSearchEnum.COUNTRY)
                .query(q)
                .build());
    }

    @GetMapping("/states")
    @Operation(summary = "Buscar estados por país e prefixo")
    public SearchLocationOutput searchStates(
            @Parameter(description = "Nome do país", example = "Brasil")
            @RequestParam String country,
            @Parameter(description = "Texto para filtro por prefixo", example = "Mat")
            @RequestParam(required = false) String q) {
        return useCase.executar(SearchLocationInput.builder()
                .type(LocationSearchEnum.STATE)
                .country(country)
                .query(q)
                .build());
    }

    @GetMapping("/cities")
    @Operation(summary = "Buscar cidades por país, estado e prefixo")
    public SearchLocationOutput searchCities(
            @Parameter(description = "Nome do país", example = "Brasil")
            @RequestParam String country,
            @Parameter(description = "Nome do estado", example = "Mato Grosso do Sul")
            @RequestParam String state,
            @Parameter(description = "Texto para filtro por prefixo", example = "Cam")
            @RequestParam(required = false) String q) {
        return useCase.executar(SearchLocationInput.builder()
                .type(LocationSearchEnum.CITY)
                .country(country)
                .state(state)
                .query(q)
                .build());
    }
}

package controller;

import adapter.entrypoint.controller.location.search.SearchLocationController;
import application.usecase.location.search.LocationSearchEnum;
import application.usecase.location.search.SearchLocationOutput;
import application.usecase.location.search.SearchLocationUseCase;
import domain.dto.LocationOptionDto;
import domain.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationControllerTest {

    @Test
    void shouldReturnCountries() throws Exception {
        SearchLocationUseCase fakeUseCase = inputData -> {
            if (inputData.getType() == LocationSearchEnum.COUNTRY) {
                return SearchLocationOutput.builder()
                        .items(List.of(LocationOptionDto.builder().name("Brasil").build()))
                        .build();
            }
            return SearchLocationOutput.builder().build();
        };

        SearchLocationController controller = new SearchLocationController(fakeUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/locations/countries")
                        .param("q", "Bra")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Brasil"));
    }
}

package application.usecase.weather.search;

import adapter.port.WeatherSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchWeatherUseCaseImpl implements SearchWeatherUseCase {

    private final WeatherSearchPort dataProvider;

    @Override
    public SearchWeatherOutput executar(SearchWeatherInput inputData) {
        return dataProvider.buscar(inputData);
    }
}

package adapter.port;

import application.usecase.weather.search.SearchWeatherInput;
import application.usecase.weather.search.SearchWeatherOutput;

public interface WeatherSearchPort {

    SearchWeatherOutput buscar(SearchWeatherInput inputData);
}

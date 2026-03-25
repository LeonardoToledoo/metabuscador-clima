package com.metaclima.metabuscador.service;

import com.metaclima.metabuscador.client.GeocodingClient;
import com.metaclima.metabuscador.client.OpenMeteoClient;
import com.metaclima.metabuscador.dto.GeoResponse;
import com.metaclima.metabuscador.dto.OpenMeteoResponse;
import com.metaclima.metabuscador.dto.WeatherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WeatherService {

    private final GeocodingClient geocodingClient;
    private final OpenMeteoClient openMeteoClient;

    public WeatherService(GeocodingClient geocodingClient, OpenMeteoClient openMeteoClient) {
        this.geocodingClient = geocodingClient;
        this.openMeteoClient = openMeteoClient;
    }

    public WeatherResponse getWeather(String city) {
        String normalizedCity = city == null ? "" : city.trim();

        if (normalizedCity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O parametro city e obrigatorio.");
        }

        GeoResponse geoResponse;
        try {
            geoResponse = geocodingClient.getCoordinates(normalizedCity);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Falha ao consultar o servico de geolocalizacao.",
                exception
            );
        }

        if (geoResponse == null || geoResponse.getResults().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cidade nao encontrada.");
        }

        GeoResponse.Result location = geoResponse.getResults().get(0);
        if (location.getLatitude() == null || location.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Coordenadas da cidade indisponiveis.");
        }

        OpenMeteoResponse openMeteoResponse;
        try {
            openMeteoResponse = openMeteoClient.getWeather(location.getLatitude(), location.getLongitude());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Falha ao consultar o servico de clima.",
                exception
            );
        }

        if (openMeteoResponse == null || openMeteoResponse.getCurrentWeather() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dados de clima indisponiveis.");
        }

        OpenMeteoResponse.CurrentWeather currentWeather = openMeteoResponse.getCurrentWeather();
        if (currentWeather.getTemperature() == null || currentWeather.getWindSpeed() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dados de clima incompletos.");
        }

        String responseCity = location.getName() == null || location.getName().isBlank()
            ? normalizedCity
            : location.getName();

        return new WeatherResponse(responseCity, currentWeather.getTemperature(), currentWeather.getWindSpeed());
    }
}

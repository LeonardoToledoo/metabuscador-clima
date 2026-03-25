package com.metaclima.metabuscador.service;

import com.metaclima.metabuscador.client.GeocodingClient;
import com.metaclima.metabuscador.client.OpenMeteoClient;
import com.metaclima.metabuscador.client.WeatherApiClient;
import com.metaclima.metabuscador.dto.GeoResponse;
import com.metaclima.metabuscador.dto.OpenMeteoResponse;
import com.metaclima.metabuscador.dto.WeatherApiResponse;
import com.metaclima.metabuscador.dto.WeatherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WeatherService {

    private final GeocodingClient geocodingClient;
    private final OpenMeteoClient openMeteoClient;
    private final WeatherApiClient weatherApiClient;

    public WeatherService(
        GeocodingClient geocodingClient,
        OpenMeteoClient openMeteoClient,
        WeatherApiClient weatherApiClient
    ) {
        this.geocodingClient = geocodingClient;
        this.openMeteoClient = openMeteoClient;
        this.weatherApiClient = weatherApiClient;
    }

    public WeatherResponse getWeather(String city) {
        String normalizedCity = normalizeCity(city);
        SourceResult openMeteoResult = loadOpenMeteoData(normalizedCity);
        SourceResult weatherApiResult = loadWeatherApiData(normalizedCity);

        WeatherData openMeteoData = openMeteoResult.data();
        WeatherData weatherApiData = weatherApiResult.data();

        if (openMeteoData == null && weatherApiData == null) {
            if (openMeteoResult.notFound() || weatherApiResult.notFound()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cidade nao encontrada.");
            }

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dados de clima indisponiveis.");
        }

        Double temperature = resolveTemperature(openMeteoData, weatherApiData);
        Double windSpeed = resolveWindSpeed(openMeteoData, weatherApiData);

        if (temperature == null || windSpeed == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dados de clima incompletos.");
        }

        String responseCity = resolveCity(normalizedCity, openMeteoData, weatherApiData);
        return new WeatherResponse(responseCity, temperature, windSpeed);
    }

    private String normalizeCity(String city) {
        String normalizedCity = city == null ? "" : city.trim();

        if (normalizedCity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O parametro city e obrigatorio.");
        }

        return normalizedCity;
    }

    private SourceResult loadOpenMeteoData(String city) {
        GeoResponse geoResponse;
        try {
            geoResponse = geocodingClient.getCoordinates(city);
        } catch (RestClientException exception) {
            return SourceResult.failed();
        }

        if (geoResponse == null || geoResponse.getResults().isEmpty()) {
            return SourceResult.notFound();
        }

        GeoResponse.Result location = geoResponse.getResults().get(0);
        if (location.getLatitude() == null || location.getLongitude() == null) {
            return SourceResult.failed();
        }

        OpenMeteoResponse openMeteoResponse;
        try {
            openMeteoResponse = openMeteoClient.getWeather(location.getLatitude(), location.getLongitude());
        } catch (RestClientException exception) {
            return SourceResult.failed();
        }

        if (openMeteoResponse == null || openMeteoResponse.getCurrentWeather() == null) {
            return SourceResult.failed();
        }

        OpenMeteoResponse.CurrentWeather currentWeather = openMeteoResponse.getCurrentWeather();
        if (currentWeather.getTemperature() == null && currentWeather.getWindSpeed() == null) {
            return SourceResult.failed();
        }

        String responseCity = hasText(location.getName()) ? location.getName() : city;
        return SourceResult.success(
            new WeatherData(responseCity, currentWeather.getTemperature(), currentWeather.getWindSpeed())
        );
    }

    private SourceResult loadWeatherApiData(String city) {
        WeatherApiResponse weatherApiResponse;
        try {
            weatherApiResponse = weatherApiClient.getCurrentWeather(city);
        } catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();
            if (statusCode == 400 || statusCode == 404) {
                return SourceResult.notFound();
            }

            return SourceResult.failed();
        } catch (RestClientException exception) {
            return SourceResult.failed();
        }

        if (weatherApiResponse == null) {
            return SourceResult.skipped();
        }

        WeatherApiResponse.Current current = weatherApiResponse.getCurrent();
        if (current == null || (current.getTemperature() == null && current.getWindSpeed() == null)) {
            return SourceResult.failed();
        }

        WeatherApiResponse.Location location = weatherApiResponse.getLocation();
        String responseCity = location != null && hasText(location.getName()) ? location.getName() : city;
        return SourceResult.success(new WeatherData(responseCity, current.getTemperature(), current.getWindSpeed()));
    }

    private Double resolveTemperature(WeatherData openMeteoData, WeatherData weatherApiData) {
        Double openMeteoTemperature = openMeteoData == null ? null : openMeteoData.temperature();
        Double weatherApiTemperature = weatherApiData == null ? null : weatherApiData.temperature();

        if (openMeteoTemperature != null && weatherApiTemperature != null) {
            return (openMeteoTemperature + weatherApiTemperature) / 2;
        }

        return openMeteoTemperature != null ? openMeteoTemperature : weatherApiTemperature;
    }

    private Double resolveWindSpeed(WeatherData openMeteoData, WeatherData weatherApiData) {
        if (openMeteoData != null && openMeteoData.windSpeed() != null) {
            return openMeteoData.windSpeed();
        }

        return weatherApiData == null ? null : weatherApiData.windSpeed();
    }

    private String resolveCity(String requestedCity, WeatherData openMeteoData, WeatherData weatherApiData) {
        if (openMeteoData != null && hasText(openMeteoData.city())) {
            return openMeteoData.city();
        }

        if (weatherApiData != null && hasText(weatherApiData.city())) {
            return weatherApiData.city();
        }

        return requestedCity;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record WeatherData(String city, Double temperature, Double windSpeed) {
    }

    private record SourceResult(WeatherData data, boolean notFound, boolean failed) {

        private static SourceResult success(WeatherData data) {
            return new SourceResult(data, false, false);
        }

        private static SourceResult notFound() {
            return new SourceResult(null, true, false);
        }

        private static SourceResult failed() {
            return new SourceResult(null, false, true);
        }

        private static SourceResult skipped() {
            return new SourceResult(null, false, false);
        }
    }
}

package com.metaclima.metabuscador.client;

import com.metaclima.metabuscador.dto.WeatherApiResponse;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherApiClient {

    private static final String WEATHER_API_URL = "http://api.weatherapi.com/v1/current.json";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public WeatherApiClient(RestTemplate restTemplate, @Value("${weatherapi.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public WeatherApiResponse getCurrentWeather(String city) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(city)) {
            return null;
        }

        URI uri = UriComponentsBuilder.fromUriString(WEATHER_API_URL)
            .queryParam("key", apiKey)
            .queryParam("q", city)
            .build()
            .encode()
            .toUri();

        return restTemplate.getForObject(uri, WeatherApiResponse.class);
    }
}

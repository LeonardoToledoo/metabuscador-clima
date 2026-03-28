package com.metaclima.metabuscador.client;

import com.metaclima.metabuscador.dto.OpenMeteoResponse;
import java.net.URI;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenMeteoClient {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";

    private final RestTemplate restTemplate;

    public OpenMeteoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OpenMeteoResponse getWeather(double latitude, double longitude) {
        URI uri = UriComponentsBuilder.fromUriString(OPEN_METEO_URL)
            .queryParam("latitude", latitude)
            .queryParam("longitude", longitude)
            .queryParam("current_weather", true)
            .build()
            .encode()
            .toUri();

        return restTemplate.getForObject(uri, OpenMeteoResponse.class);
    }
}

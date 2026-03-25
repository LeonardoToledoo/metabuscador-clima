package com.metaclima.metabuscador.client;

import com.metaclima.metabuscador.dto.GeoResponse;
import java.net.URI;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GeocodingClient {

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";

    private final RestTemplate restTemplate;

    public GeocodingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeoResponse getCoordinates(String city) {
        URI uri = UriComponentsBuilder.fromUriString(GEOCODING_URL)
            .queryParam("name", city)
            .queryParam("count", 1)
            .queryParam("language", "pt")
            .queryParam("format", "json")
            .build()
            .encode()
            .toUri();

        return restTemplate.getForObject(uri, GeoResponse.class);
    }
}

package adapter.outbound.client;

import application.config.WeatherApiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherApiClient {

    private final RestClient restClient;
    private final WeatherApiProperties properties;

    public WeatherApiClient(@Qualifier("weatherApiRestClient") RestClient restClient, WeatherApiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public WeatherApiForecastResponse getForecast(double latitude, double longitude, int days) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast.json")
                        .queryParam("key", properties.getKey())
                        .queryParam("q", latitude + "," + longitude)
                        .queryParam("days", days)
                        .queryParam("aqi", "no")
                        .queryParam("alerts", "no")
                        .queryParam("lang", "pt")
                        .build())
                .retrieve()
                .body(WeatherApiForecastResponse.class);
    }
}

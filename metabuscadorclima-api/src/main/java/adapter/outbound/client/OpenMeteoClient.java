package adapter.outbound.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private final RestClient geocodingClient;
    private final RestClient forecastClient;

    public OpenMeteoClient(
            @Qualifier("openMeteoGeocodingRestClient") RestClient geocodingClient,
            @Qualifier("openMeteoForecastRestClient") RestClient forecastClient) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
    }

    public OpenMeteoGeocodingResponse geocodeCity(String city) {
        return geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 10)
                        .queryParam("language", "pt")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(OpenMeteoGeocodingResponse.class);
    }

    public OpenMeteoForecastResponse getForecast(double latitude, double longitude, int days) {
        return forecastClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,visibility,surface_pressure")
                        .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min")
                        .queryParam("timezone", "auto")
                        .queryParam("forecast_days", days)
                        .build())
                .retrieve()
                .body(OpenMeteoForecastResponse.class);
    }
}

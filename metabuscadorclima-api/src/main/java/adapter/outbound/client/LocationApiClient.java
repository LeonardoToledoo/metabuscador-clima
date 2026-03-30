package adapter.outbound.client;

import application.config.LocationApiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LocationApiClient {

    private final LocationApiProperties properties;
    private final RestClient restClient;

    public LocationApiClient(
            LocationApiProperties properties,
            @Qualifier("locationApiRestClient") RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    public CountryResponse[] getCountries() {
        return restClient.get()
                .uri("/countries")
                .header("X-CSCAPI-KEY", properties.getKey())
                .retrieve()
                .body(CountryResponse[].class);
    }

    public StateResponse[] getStates(String countryIso2) {
        return restClient.get()
                .uri("/countries/{countryIso2}/states", countryIso2)
                .header("X-CSCAPI-KEY", properties.getKey())
                .retrieve()
                .body(StateResponse[].class);
    }

    public CityResponse[] getCities(String countryIso2, String stateIso2) {
        return restClient.get()
                .uri("/countries/{countryIso2}/states/{stateIso2}/cities", countryIso2, stateIso2)
                .header("X-CSCAPI-KEY", properties.getKey())
                .retrieve()
                .body(CityResponse[].class);
    }

    public record CountryResponse(String name, String iso2) {}

    public record StateResponse(String name, String iso2) {}

    public record CityResponse(Long id, String name) {}
}

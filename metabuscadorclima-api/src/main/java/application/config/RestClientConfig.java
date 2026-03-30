package application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("openMeteoGeocodingRestClient")
    public RestClient openMeteoGeocodingRestClient(OpenMeteoProperties properties, TimeoutProperties timeoutProperties) {
        return RestClient.builder()
                .baseUrl(properties.getGeocodingBaseUrl())
                .requestFactory(requestFactory(timeoutProperties))
                .build();
    }

    @Bean
    @Qualifier("openMeteoForecastRestClient")
    public RestClient openMeteoForecastRestClient(OpenMeteoProperties properties, TimeoutProperties timeoutProperties) {
        return RestClient.builder()
                .baseUrl(properties.getForecastBaseUrl())
                .requestFactory(requestFactory(timeoutProperties))
                .build();
    }

    @Bean
    @Qualifier("weatherApiRestClient")
    public RestClient weatherApiRestClient(WeatherApiProperties properties, TimeoutProperties timeoutProperties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(timeoutProperties))
                .build();
    }

    @Bean
    @Qualifier("locationApiRestClient")
    public RestClient locationApiRestClient(LocationApiProperties properties, TimeoutProperties timeoutProperties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(timeoutProperties))
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(TimeoutProperties timeoutProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutProperties.getConnectMs());
        requestFactory.setReadTimeout(timeoutProperties.getReadMs());
        return requestFactory;
    }
}

package application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "openmeteo")
public class OpenMeteoProperties {

    private String geocodingBaseUrl;
    private String forecastBaseUrl;
}

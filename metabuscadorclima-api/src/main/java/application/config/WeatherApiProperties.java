package application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "weatherapi")
public class WeatherApiProperties {

    private String baseUrl;
    private String key;
}

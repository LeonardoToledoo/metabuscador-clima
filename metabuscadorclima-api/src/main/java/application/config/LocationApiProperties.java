package application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "locationapi")
public class LocationApiProperties {

    private String baseUrl;
    private String key;
}

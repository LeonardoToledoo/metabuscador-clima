package application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "timeout")
public class TimeoutProperties {

    private int connectMs;
    private int readMs;
}

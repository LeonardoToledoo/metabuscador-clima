package application;

import application.config.CorsProperties;
import application.config.OpenMeteoProperties;
import application.config.TimeoutProperties;
import application.config.WeatherApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"application", "adapter", "domain"})
@EnableConfigurationProperties({
        WeatherApiProperties.class,
        OpenMeteoProperties.class,
        TimeoutProperties.class,
        CorsProperties.class
})
public class MetaClimaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaClimaApplication.class, args);
    }
}

package application.service;

import application.config.WeatherApiProperties;
import domain.dto.ConsolidatedWeatherDto;
import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import domain.dto.SourceWeatherDto;
import domain.dto.WeatherSearchResponseDto;
import domain.exception.CityNotFoundException;
import domain.exception.ExternalServiceException;
import adapter.outbound.client.OpenMeteoClient;
import adapter.outbound.client.OpenMeteoGeocodingResponse;
import adapter.outbound.client.OpenMeteoForecastResponse;
import adapter.outbound.client.WeatherApiClient;
import adapter.outbound.client.WeatherApiForecastResponse;
import adapter.outbound.mapper.OpenMeteoMapper;
import adapter.outbound.mapper.WeatherApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WeatherAggregatorService {

    private static final int FORECAST_DAYS = 5;

    private final OpenMeteoClient openMeteoClient;
    private final WeatherApiClient weatherApiClient;
    private final OpenMeteoMapper openMeteoMapper;
    private final WeatherApiMapper weatherApiMapper;
    private final WeatherApiProperties weatherApiProperties;

    public WeatherSearchResponseDto search(String city) {
        if (!StringUtils.hasText(city)) {
            throw new IllegalArgumentException("Informe uma cidade válida para consulta.");
        }

        OpenMeteoGeocodingResponse geocoding = openMeteoClient.geocodeCity(city.trim());
        OpenMeteoGeocodingResponse.Result geoResult = extractFirstResult(geocoding);
        LocationDto baseLocation = new LocationDto(
                geoResult.getName(),
                geoResult.getAdmin1(),
                geoResult.getCountry(),
                geoResult.getLatitude(),
                geoResult.getLongitude()
        );

        List<SourceWeatherDto> sources = new ArrayList<>();
        List<NormalizedWeatherData> availableData = new ArrayList<>();

        try {
            OpenMeteoForecastResponse openMeteoResponse = openMeteoClient.getForecast(
                    geoResult.getLatitude(),
                    geoResult.getLongitude(),
                    FORECAST_DAYS
            );
            NormalizedWeatherData normalized = openMeteoMapper.toNormalized(openMeteoResponse, baseLocation);
            availableData.add(normalized);
            sources.add(new SourceWeatherDto(
                    normalized.getSource(),
                    true,
                    "Dados carregados com sucesso.",
                    normalized.getCurrent(),
                    normalized.getForecast()
            ));
        } catch (RestClientException | IllegalArgumentException ex) {
            sources.add(new SourceWeatherDto(
                    "Open-Meteo",
                    false,
                    "Falha ao consultar Open-Meteo.",
                    null,
                    List.of()
            ));
        }

        if (StringUtils.hasText(weatherApiProperties.getKey())) {
            try {
                WeatherApiForecastResponse weatherApiResponse = weatherApiClient.getForecast(
                        geoResult.getLatitude(),
                        geoResult.getLongitude(),
                        FORECAST_DAYS
                );
                NormalizedWeatherData normalized = weatherApiMapper.toNormalized(weatherApiResponse);
                availableData.add(normalized);
                sources.add(new SourceWeatherDto(
                        normalized.getSource(),
                        true,
                        "Dados carregados com sucesso.",
                        normalized.getCurrent(),
                        normalized.getForecast()
                ));
            } catch (RestClientException | IllegalArgumentException ex) {
                sources.add(new SourceWeatherDto(
                        "WeatherAPI",
                        false,
                        "Falha ao consultar WeatherAPI.",
                        null,
                        List.of()
                ));
            }
        } else {
            sources.add(new SourceWeatherDto(
                    "WeatherAPI",
                    false,
                    "WEATHERAPI_KEY não configurada.",
                    null,
                    List.of()
            ));
        }

        if (availableData.isEmpty()) {
            throw new ExternalServiceException("Ocorreu um erro ao consultar as fontes climáticas.");
        }

        ConsolidatedWeatherDto consolidated = consolidateCurrent(availableData);
        List<ForecastDayDto> consolidatedForecast = consolidateForecast(availableData);

        return new WeatherSearchResponseDto(city.trim(), baseLocation, sources, consolidated, consolidatedForecast);
    }

    private OpenMeteoGeocodingResponse.Result extractFirstResult(OpenMeteoGeocodingResponse geocoding) {
        if (Objects.isNull(geocoding) || Objects.isNull(geocoding.getResults()) || geocoding.getResults().isEmpty()) {
            throw new CityNotFoundException("Não foi possível encontrar a cidade informada.");
        }
        return geocoding.getResults().get(0);
    }

    public ConsolidatedWeatherDto consolidateCurrent(List<NormalizedWeatherData> entries) {
        List<Double> temperatures = new ArrayList<>();
        List<Double> feelsLike = new ArrayList<>();
        List<Integer> humidities = new ArrayList<>();
        List<Double> winds = new ArrayList<>();
        List<Double> visibilities = new ArrayList<>();
        List<Double> pressures = new ArrayList<>();
        List<Double> maxTemps = new ArrayList<>();
        List<Double> minTemps = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        Set<String> sourcesUsed = new LinkedHashSet<>();

        for (NormalizedWeatherData data : entries) {
            if (Objects.isNull(data) || Objects.isNull(data.getCurrent())) {
                continue;
            }
            CurrentWeatherDto c = data.getCurrent();
            if (Objects.nonNull(c.getTemperatureC())) temperatures.add(c.getTemperatureC());
            if (Objects.nonNull(c.getFeelsLikeC())) feelsLike.add(c.getFeelsLikeC());
            if (Objects.nonNull(c.getHumidity())) humidities.add(c.getHumidity());
            if (Objects.nonNull(c.getWindKph())) winds.add(c.getWindKph());
            if (Objects.nonNull(c.getVisibilityKm())) visibilities.add(c.getVisibilityKm());
            if (Objects.nonNull(c.getPressureHpa())) pressures.add(c.getPressureHpa());
            if (Objects.nonNull(c.getMaxTempC())) maxTemps.add(c.getMaxTempC());
            if (Objects.nonNull(c.getMinTempC())) minTemps.add(c.getMinTempC());
            if (Objects.nonNull(c.getCondition()) && !c.getCondition().isBlank()) conditions.add(c.getCondition());
            sourcesUsed.add(data.getSource());
        }

        CurrentWeatherDto current = new CurrentWeatherDto(
                averageDouble(temperatures),
                averageDouble(feelsLike),
                averageInteger(humidities),
                averageDouble(winds),
                averageDouble(visibilities),
                averageDouble(pressures),
                conditions.isEmpty() ? "Condição não informada" : conditions.get(0),
                averageDouble(maxTemps),
                averageDouble(minTemps)
        );

        return new ConsolidatedWeatherDto(new ArrayList<>(sourcesUsed), current);
    }

    private List<ForecastDayDto> consolidateForecast(List<NormalizedWeatherData> entries) {
        int maxDays = entries.stream()
                .mapToInt(entry -> entry.getForecast() != null ? entry.getForecast().size() : 0)
                .max()
                .orElse(0);

        List<ForecastDayDto> consolidated = new ArrayList<>();
        for (int i = 0; i < maxDays; i++) {
            String date = null;
            List<Double> minTemps = new ArrayList<>();
            List<Double> maxTemps = new ArrayList<>();
            String condition = null;

            for (NormalizedWeatherData entry : entries) {
                if (Objects.isNull(entry.getForecast()) || i >= entry.getForecast().size()) {
                    continue;
                }
                ForecastDayDto day = entry.getForecast().get(i);
                if (Objects.isNull(date)) date = day.getDate();
                if (Objects.nonNull(day.getMinTempC())) minTemps.add(day.getMinTempC());
                if (Objects.nonNull(day.getMaxTempC())) maxTemps.add(day.getMaxTempC());
                if (Objects.isNull(condition) && Objects.nonNull(day.getCondition()) && !day.getCondition().isBlank()) {
                    condition = day.getCondition();
                }
            }

            consolidated.add(new ForecastDayDto(
                    date,
                    averageDouble(minTemps),
                    averageDouble(maxTemps),
                    condition != null ? condition : "Condição não informada"
            ));
        }

        return consolidated;
    }

    private Double averageDouble(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Integer averageInteger(List<Integer> values) {
        if (values.isEmpty()) {
            return null;
        }
        return (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(0.0));
    }
}

package adapter.outbound.provider;

import application.config.WeatherApiProperties;
import adapter.port.WeatherSearchPort;
import application.usecase.weather.search.SearchWeatherInput;
import application.usecase.weather.search.SearchWeatherOutput;
import domain.dto.ConsolidatedWeatherDto;
import domain.dto.CurrentWeatherDto;
import domain.dto.ForecastDayDto;
import domain.dto.LocationDto;
import domain.dto.NormalizedWeatherData;
import domain.dto.SourceWeatherDto;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ExternalWeatherSearchProvider implements WeatherSearchPort {

    private static final int FORECAST_DAYS = 5;

    private final OpenMeteoClient openMeteoClient;
    private final WeatherApiClient weatherApiClient;
    private final OpenMeteoMapper openMeteoMapper;
    private final WeatherApiMapper weatherApiMapper;
    private final WeatherApiProperties weatherApiProperties;

    @Override
    public SearchWeatherOutput buscar(SearchWeatherInput inputData) {
        String rawQuery = inputData.getCity();
        if (!StringUtils.hasText(rawQuery)) {
            throw new IllegalArgumentException("Informe uma cidade válida para consulta.");
        }

        ParsedLocationQuery parsedQuery = parseLocationQuery(rawQuery);
        OpenMeteoGeocodingResponse geocoding = openMeteoClient.geocodeCity(parsedQuery.city());
        OpenMeteoGeocodingResponse.Result geoResult = extractBestResult(geocoding, parsedQuery);
        LocationDto baseLocation = LocationDto.builder()
                .name(geoResult.getName())
                .region(geoResult.getAdmin1())
                .country(geoResult.getCountry())
                .latitude(geoResult.getLatitude())
                .longitude(geoResult.getLongitude())
                .build();

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
            sources.add(SourceWeatherDto.builder()
                    .source(normalized.getSource())
                    .available(true)
                    .message("Dados carregados com sucesso.")
                    .current(normalized.getCurrent())
                    .forecast(normalized.getForecast())
                    .build());
        } catch (RestClientException | IllegalArgumentException ex) {
            sources.add(SourceWeatherDto.builder()
                    .source("Open-Meteo")
                    .available(false)
                    .message("Falha ao consultar Open-Meteo.")
                    .forecast(List.of())
                    .build());
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
                sources.add(SourceWeatherDto.builder()
                        .source(normalized.getSource())
                        .available(true)
                        .message("Dados carregados com sucesso.")
                        .current(normalized.getCurrent())
                        .forecast(normalized.getForecast())
                        .build());
            } catch (RestClientException | IllegalArgumentException ex) {
                sources.add(SourceWeatherDto.builder()
                        .source("WeatherAPI")
                        .available(false)
                        .message("Falha ao consultar WeatherAPI.")
                        .forecast(List.of())
                        .build());
            }
        } else {
            sources.add(SourceWeatherDto.builder()
                    .source("WeatherAPI")
                    .available(false)
                    .message("WEATHERAPI_KEY não configurada.")
                    .forecast(List.of())
                    .build());
        }

        if (availableData.isEmpty()) {
            throw new ExternalServiceException("Ocorreu um erro ao consultar as fontes climáticas.");
        }

        ConsolidatedWeatherDto consolidated = consolidateCurrent(availableData);
        List<ForecastDayDto> consolidatedForecast = consolidateForecast(availableData);

        return SearchWeatherOutput.builder()
                .query(rawQuery.trim())
                .location(baseLocation)
                .sources(sources)
                .consolidated(consolidated)
                .forecast(consolidatedForecast)
                .build();
    }

    private OpenMeteoGeocodingResponse.Result extractBestResult(OpenMeteoGeocodingResponse geocoding, ParsedLocationQuery parsedQuery) {
        if (Objects.isNull(geocoding) || Objects.isNull(geocoding.getResults()) || geocoding.getResults().isEmpty()) {
            throw new CityNotFoundException("Não foi possível encontrar a cidade informada.");
        }

        return geocoding.getResults().stream()
                .filter(result -> matchesLocation(result, parsedQuery))
                .findFirst()
                .orElse(geocoding.getResults().get(0));
    }

    private ParsedLocationQuery parseLocationQuery(String rawQuery) {
        String[] parts = rawQuery.split(",");
        String city = parts.length > 0 ? parts[0].trim() : rawQuery.trim();
        String state = parts.length > 1 ? parts[1].trim() : null;
        String country = parts.length > 2 ? parts[2].trim() : null;
        return new ParsedLocationQuery(city, emptyToNull(state), emptyToNull(country));
    }

    private boolean matchesLocation(OpenMeteoGeocodingResponse.Result result, ParsedLocationQuery parsedQuery) {
        if (!normalizedEquals(result.getName(), parsedQuery.city())) {
            return false;
        }

        boolean stateMatches = !StringUtils.hasText(parsedQuery.state())
                || normalizedEquals(result.getAdmin1(), parsedQuery.state());

        boolean countryMatches = !StringUtils.hasText(parsedQuery.country())
                || normalizedCountryEquals(result.getCountry(), parsedQuery.country());

        return stateMatches && countryMatches;
    }

    private boolean normalizedEquals(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private boolean normalizedCountryEquals(String left, String right) {
        String normalizedLeft = normalizeCountry(left);
        String normalizedRight = normalizeCountry(right);
        return normalizedLeft.equals(normalizedRight);
    }

    private String normalizeCountry(String value) {
        String normalized = normalize(value);
        if ("brasil".equals(normalized)) {
            return "brazil";
        }
        return normalized;
    }

    private String normalize(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private ConsolidatedWeatherDto consolidateCurrent(List<NormalizedWeatherData> entries) {
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
            CurrentWeatherDto current = data.getCurrent();
            if (Objects.nonNull(current.getTemperatureC())) temperatures.add(current.getTemperatureC());
            if (Objects.nonNull(current.getFeelsLikeC())) feelsLike.add(current.getFeelsLikeC());
            if (Objects.nonNull(current.getHumidity())) humidities.add(current.getHumidity());
            if (Objects.nonNull(current.getWindKph())) winds.add(current.getWindKph());
            if (Objects.nonNull(current.getVisibilityKm())) visibilities.add(current.getVisibilityKm());
            if (Objects.nonNull(current.getPressureHpa())) pressures.add(current.getPressureHpa());
            if (Objects.nonNull(current.getMaxTempC())) maxTemps.add(current.getMaxTempC());
            if (Objects.nonNull(current.getMinTempC())) minTemps.add(current.getMinTempC());
            if (Objects.nonNull(current.getCondition()) && !current.getCondition().isBlank()) conditions.add(current.getCondition());
            sourcesUsed.add(data.getSource());
        }

        CurrentWeatherDto consolidatedCurrent = CurrentWeatherDto.builder()
                .temperatureC(averageDouble(temperatures))
                .feelsLikeC(averageDouble(feelsLike))
                .humidity(averageInteger(humidities))
                .windKph(averageDouble(winds))
                .visibilityKm(averageDouble(visibilities))
                .pressureHpa(averageDouble(pressures))
                .condition(conditions.isEmpty() ? "Condição não informada" : conditions.get(0))
                .maxTempC(averageDouble(maxTemps))
                .minTempC(averageDouble(minTemps))
                .build();

        return ConsolidatedWeatherDto.builder()
                .sourcesUsed(new ArrayList<>(sourcesUsed))
                .current(consolidatedCurrent)
                .build();
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

            consolidated.add(ForecastDayDto.builder()
                    .date(date)
                    .minTempC(averageDouble(minTemps))
                    .maxTempC(averageDouble(maxTemps))
                    .condition(condition != null ? condition : "Condição não informada")
                    .build());
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

    private record ParsedLocationQuery(String city, String state, String country) {
    }
}

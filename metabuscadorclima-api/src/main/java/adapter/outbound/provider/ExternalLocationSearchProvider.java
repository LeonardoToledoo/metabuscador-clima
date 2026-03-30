package adapter.outbound.provider;

import application.config.LocationApiProperties;
import adapter.port.LocationSearchPort;
import application.usecase.location.search.SearchLocationInput;
import application.usecase.location.search.SearchLocationOutput;
import domain.dto.LocationOptionDto;
import adapter.outbound.client.LocationApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ExternalLocationSearchProvider implements LocationSearchPort {

    private final LocationApiClient locationApiClient;
    private final LocationApiProperties locationApiProperties;
    private final LocalLocationCatalog localLocationCatalog;

    private final Map<String, String> countryIsoCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> stateIsoCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> statesCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> citiesCache = new ConcurrentHashMap<>();
    private volatile List<String> countryCache;

    @Override
    public SearchLocationOutput buscar(SearchLocationInput inputData) {
        List<LocationOptionDto> items = switch (inputData.getType()) {
            case COUNTRY -> searchCountries(inputData.getQuery());
            case STATE -> searchStates(inputData.getCountry(), inputData.getQuery());
            case CITY -> searchCities(inputData.getCountry(), inputData.getState(), inputData.getQuery());
        };

        return SearchLocationOutput.builder()
                .items(items)
                .build();
    }

    private List<LocationOptionDto> searchCountries(String query) {
        List<String> countries = getCountries();
        return filterNames(countries, query).stream()
                .map(name -> LocationOptionDto.builder().name(name).build())
                .toList();
    }

    private List<LocationOptionDto> searchStates(String country, String query) {
        if (!StringUtils.hasText(country)) {
            return List.of();
        }

        List<String> states = getStates(country);
        return filterNames(states, query).stream()
                .map(name -> LocationOptionDto.builder().name(name).build())
                .toList();
    }

    private List<LocationOptionDto> searchCities(String country, String state, String query) {
        if (!StringUtils.hasText(country) || !StringUtils.hasText(state)) {
            return List.of();
        }

        List<String> cities = getCities(country, state);
        return filterNames(cities, query).stream()
                .map(name -> LocationOptionDto.builder().name(name).build())
                .toList();
    }

    private List<String> getCountries() {
        if (!StringUtils.hasText(locationApiProperties.getKey())) {
            return getLocalCountries();
        }

        if (countryCache != null) {
            return countryCache;
        }

        try {
            LocationApiClient.CountryResponse[] response = locationApiClient.getCountries();
            List<String> countries = new ArrayList<>();
            countryIsoCache.clear();
            for (LocationApiClient.CountryResponse item : response) {
                countries.add(item.name());
                countryIsoCache.put(item.name(), item.iso2());
            }
            countryCache = countries.stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            return countryCache;
        } catch (RestClientException ex) {
            return getLocalCountries();
        }
    }

    private List<String> getStates(String country) {
        if (!StringUtils.hasText(locationApiProperties.getKey())) {
            return getLocalStates(country);
        }

        if (statesCache.containsKey(country)) {
            return statesCache.get(country);
        }

        String countryIso2 = resolveCountryIso2(country);
        if (!StringUtils.hasText(countryIso2)) {
            return getLocalStates(country);
        }

        try {
            LocationApiClient.StateResponse[] response = locationApiClient.getStates(countryIso2);
            Map<String, String> states = new ConcurrentHashMap<>();
            List<String> names = new ArrayList<>();
            for (LocationApiClient.StateResponse item : response) {
                states.put(item.name(), item.iso2());
                names.add(item.name());
            }
            stateIsoCache.put(country, states);
            List<String> sortedNames = names.stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            statesCache.put(country, sortedNames);
            return sortedNames;
        } catch (RestClientException ex) {
            return getLocalStates(country);
        }
    }

    private List<String> getCities(String country, String state) {
        if (!StringUtils.hasText(locationApiProperties.getKey())) {
            return getLocalCities(country, state);
        }

        String cityCacheKey = country + "::" + state;
        if (citiesCache.containsKey(cityCacheKey)) {
            return citiesCache.get(cityCacheKey);
        }

        String countryIso2 = resolveCountryIso2(country);
        String stateIso2 = resolveStateIso2(country, state);
        if (!StringUtils.hasText(countryIso2) || !StringUtils.hasText(stateIso2)) {
            return getLocalCities(country, state);
        }

        try {
            LocationApiClient.CityResponse[] response = locationApiClient.getCities(countryIso2, stateIso2);
            List<String> cities = new ArrayList<>();
            for (LocationApiClient.CityResponse item : response) {
                cities.add(item.name());
            }
            List<String> sortedCities = cities.stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
            citiesCache.put(cityCacheKey, sortedCities);
            return sortedCities;
        } catch (RestClientException ex) {
            return getLocalCities(country, state);
        }
    }

    private String resolveCountryIso2(String country) {
        getCountries();
        return countryIsoCache.get(country);
    }

    private String resolveStateIso2(String country, String state) {
        Map<String, String> states = stateIsoCache.computeIfAbsent(country, key -> {
            getStates(country);
            return stateIsoCache.getOrDefault(country, Map.of());
        });
        return states.get(state);
    }

    private List<String> getLocalCountries() {
        return localLocationCatalog.getCountries().stream()
                .map(LocalLocationCatalog.CountryCatalogItem::name)
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    private List<String> getLocalStates(String country) {
        return localLocationCatalog.getCountries().stream()
                .filter(item -> item.name().equalsIgnoreCase(country))
                .findFirst()
                .map(item -> item.states().stream()
                        .map(LocalLocationCatalog.StateCatalogItem::name)
                        .sorted(String::compareToIgnoreCase)
                        .toList())
                .orElse(List.of());
    }

    private List<String> getLocalCities(String country, String state) {
        return localLocationCatalog.getCountries().stream()
                .filter(item -> item.name().equalsIgnoreCase(country))
                .findFirst()
                .flatMap(item -> item.states().stream()
                        .filter(stateItem -> stateItem.name().equalsIgnoreCase(state))
                        .findFirst())
                .map(item -> item.cities().stream()
                        .sorted(String::compareToIgnoreCase)
                        .toList())
                .orElse(List.of());
    }

    private List<String> filterNames(List<String> options, String query) {
        String normalizedQuery = normalize(query);
        if (!StringUtils.hasText(normalizedQuery)) {
            return options.stream()
                    .sorted(String::compareToIgnoreCase)
                    .toList();
        }

        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String option : options) {
            String normalizedOption = normalize(option);
            if (normalizedOption.startsWith(normalizedQuery)) {
                startsWith.add(option);
            } else if (normalizedOption.contains(normalizedQuery)) {
                contains.add(option);
            }
        }

        startsWith.sort(String::compareToIgnoreCase);
        contains.sort(String::compareToIgnoreCase);
        startsWith.addAll(contains);
        return startsWith;
    }

    private String normalize(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}

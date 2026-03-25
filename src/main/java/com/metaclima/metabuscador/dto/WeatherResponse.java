package com.metaclima.metabuscador.dto;

public class WeatherResponse {

    private String city;
    private Double temperature;
    private Double windSpeed;

    public WeatherResponse() {
    }

    public WeatherResponse(String city, Double temperature, Double windSpeed) {
        this.city = city;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }
}

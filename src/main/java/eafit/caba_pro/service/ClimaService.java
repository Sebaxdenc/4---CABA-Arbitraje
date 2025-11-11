package eafit.caba_pro.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

@Service
@Slf4j
public class ClimaService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getWeather(double latitude, double longitude, LocalDate date) {
        String url = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "temperature_2m,weather_code")
                .queryParam("timezone", "America/New_York")
                .queryParam("start_date", date)
                .queryParam("end_date", date)
                .toUriString();
        log.info(url);
        return restTemplate.getForObject(url, Map.class);
    }

    public String mapCode(Integer code) {
        String clima;

        if (code == 0) {
            clima = "Despejado";
        } 
        else if (code >= 1 && code <= 3) {
            clima = "Parcialmente nublado";
        } 
        else if ((code >= 45 && code <= 48) || (code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            clima = "Lluvioso";
        } 
        else if ((code >= 95 && code <= 99)) {
            clima = "Tormenta";
        } 
        else if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) {
            clima = "Nevado";
        } 
        else {
            clima = "Desconocido";
        }
        return clima;
    }

}

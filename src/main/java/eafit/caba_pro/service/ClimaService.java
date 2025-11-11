package eafit.caba_pro.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClimaService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MessageSource messageSource;

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
        Locale locale = LocaleContextHolder.getLocale();
        String key;

        if (code == 0) {
            key = "weather.clear";
        } 
        else if (code >= 1 && code <= 3) {
            key = "weather.partly_cloudy";
        } 
        else if ((code >= 45 && code <= 48) || (code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            key = "weather.rainy";
        } 
        else if ((code >= 95 && code <= 99)) {
            key = "weather.stormy";
        } 
        else if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) {
            key = "weather.snowy";
        } 
        else {
            key = "weather.unknown";
        }
        
        return messageSource.getMessage(key, null, locale);
    }

}

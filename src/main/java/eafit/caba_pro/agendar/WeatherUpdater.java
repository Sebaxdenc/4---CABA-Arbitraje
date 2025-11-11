package eafit.caba_pro.agendar;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.repository.PartidoRepository;
import eafit.caba_pro.service.ClimaService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WeatherUpdater {

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ClimaService ClimaService;

    // Coordenadas fijas para Medellín
    private static final double LATITUDE = 6.263469;
    private static final double LONGITUDE = -75.577089;

    /**
     * Ejecuta cada 6 horas (expresado en milisegundos: 6 * 60 * 60 * 1000)
    */

    @Scheduled(fixedRate = 21600000)
    @EventListener(ApplicationReadyEvent.class)
    public void actualizarClimaDePartidos() {

        List<Partido> partidos = partidoRepository.findAll();

        for (Partido partido : partidos) {
            Map<String, Object> weatherData = ClimaService.getWeather(LATITUDE, LONGITUDE, partido.getFecha());

            if (weatherData != null && weatherData.containsKey("hourly")) {
                Map<String, Object> hourly = (Map<String, Object>) weatherData.get("hourly");

                int hora = partido.getHora().getHour();
                Object temperaturesObj = hourly.get("temperature_2m");
                Object codesObj = hourly.get("weather_code");
                
                if (temperaturesObj instanceof List) {
                    List<?> temperatures = (List<?>) temperaturesObj;
                    if (!temperatures.isEmpty()) {
                        Object temp = temperatures.get(hora);
                        if (temp != null) {
                            // Convertir el objeto a número
                            if (temp instanceof Number) {
                                partido.setTemperatura(((Number) temp).intValue());
                            } else {
                                // Si viene como String, parsearlo
                                partido.setTemperatura(Integer.parseInt(temp.toString()));
                            }
                        }
                    }
                }

                if (codesObj instanceof List) {
                List<?> codes = (List<?>) codesObj;
                if (!codes.isEmpty()) {
                    Object code = codes.get(hora);
                    if (code != null) {
                        if (code instanceof Number) {
                            partido.setClima(
                                ClimaService.mapCode(((Number) code).intValue())
                            );
                        } else {
                            partido.setClima(
                                ClimaService.mapCode(Integer.parseInt(code.toString()))

                            );
                        }
                    }
                }
            }

                partidoRepository.save(partido);
            }
        }

        log.info("Clima actualizado");
    }
}

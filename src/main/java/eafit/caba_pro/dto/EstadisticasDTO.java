package eafit.caba_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasDTO {
    private String equipoNombre;
    private String equipoCiudad;
    private Integer equipoFundacion;
    private Boolean equipoEstado;
    private String equipoLogo;
    private String entrenadorNombre;
    private String categoriaEntrenador;
    private Integer experienciaEntrenador;
    private Integer totalPartidos;
    private Integer partidosGanados;
    private Integer partidosPerdidos;
    private Integer partidosEmpatados;
    private Double porcentajeVictorias;
    private Double porcentajeDerrotas;
    private Double porcentajeEmpates;
}

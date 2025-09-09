package eafit.caba_pro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La fecha no puede ser nula")
    @Column(nullable = false)
    private LocalDate fecha;
    
    @NotNull(message = "La hora no puede ser nula")
    @Column(nullable = false)
    private LocalTime hora;
    
    @NotNull(message = "El equipo local no puede ser nulo")
    @NotEmpty(message = "El equipo local no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String equipoLocal;
    
    @NotNull(message = "El equipo visitante no puede ser nulo")
    @NotEmpty(message = "El equipo visitante no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String equipoVisitante;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    // Relación con el árbitro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitro_id")
    @JsonBackReference // Evitar serialización circular - lado "back"
    private Arbitro arbitro;
    
    // Enums
    public enum EstadoPartido {
        PROGRAMADO, EN_CURSO, FINALIZADO, SUSPENDIDO, CANCELADO
    }
    
    // Métodos helper
    public boolean esPartidoFuturo() {
        return fecha.isAfter(LocalDate.now()) || 
               (fecha.equals(LocalDate.now()) && hora.isAfter(LocalTime.now()));
    }
    
    public boolean esPartidoPasado() {
        return fecha.isBefore(LocalDate.now()) || 
               (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now()));
    }
    
    public String getPartidoCompleto() {
        return equipoLocal + " vs " + equipoVisitante;
    }
    
    public boolean tieneArbitro(Arbitro arbitro) {
        return (this.arbitro != null && this.arbitro.getId().equals(arbitro.getId()));
    }
    
}

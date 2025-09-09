package eafit.caba_pro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partido")
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;
    
    // Relación con el árbitro
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arbitro_id")
    @JsonBackReference // Evitar serialización circular - lado "back"
    private Arbitro arbitro;
    
    // Relación con reseñas (un partido puede tener múltiples reseñas)
    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Reseña> reseñas;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_visitante")
    //@JsonIgnore
    @JsonBackReference
    //@JsonIgnoreProperties({"partidosLocal", "partidosVisitante"})
    @NotNull(message = "El equipo visitante no puede ser nulo")
    private Equipo equipoVisitante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_local")
    //@JsonIgnore
    @JsonBackReference
    //@JsonIgnoreProperties({"partidosLocal", "partidosVisitante"})
    @NotNull(message = "El equipo local no puede ser nulo")
    private Equipo equipoLocal;
    
    // Enums
    public enum EstadoPartido {
        PROGRAMADO, EN_CURSO, FINALIZADO, PENDIENTE_CONFIRMACION, ARBITRO_NO_DISPONIBLE
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
    /*
    public String getPartidoCompleto() {
        return equipoLocal + " vs " + equipoVisitante;
    }
    */
    public boolean tieneArbitro(Arbitro arbitro) {
        return (this.arbitro != null && this.arbitro.getId().equals(arbitro.getId()));
    }
    
}

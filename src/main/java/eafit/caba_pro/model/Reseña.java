package eafit.caba_pro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "reseña")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reseña {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La puntuación no puede ser nula")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    @Column(nullable = false)
    private Integer puntuacion;
    
    @NotNull(message = "La descripción no puede ser nula")
    @NotEmpty(message = "La descripción no puede estar vacía")
    @Column(nullable = false, length = 1000)
    private String descripcion;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    // Relación con Árbitro (muchas reseñas pueden ser para un árbitro)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitro_id", nullable = false)
    @JsonBackReference
    private Arbitro arbitro;
    
    // Relación con Entrenador (muchas reseñas pueden ser de un entrenador)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", nullable = false)
    @JsonBackReference
    private Entrenador entrenador;
    
    // Relación con Partido (muchas reseñas pueden ser para un partido)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_id")
    @JsonBackReference
    private Partido partido;
    
    // Métodos helper
    public String getFechaFormateada() {
        return fechaCreacion.toLocalDate().toString();
    }
    
    public String getPuntuacionEstrellas() {
        StringBuilder estrellas = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= puntuacion) {
                estrellas.append("🏀"); // Balón de baloncesto lleno
            } else {
                estrellas.append("⚪"); // Círculo vacío
            }
        }
        return estrellas.toString();
    
    }
}

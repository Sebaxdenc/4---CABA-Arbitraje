package eafit.caba_pro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "equipo")
@Data // Generate getters and setters for all fields using lombok
@AllArgsConstructor // Generate a contrustuctor with all the fields
@NoArgsConstructor // Generates a constructor with no fields acordding to JPA
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre es necesario")
    private String nombre;

    @Column(nullable = false)
    private boolean estado;
    
    @Column(nullable = false)
    @NotBlank(message = "La ciudad es necesaria")
    private String ciudad;
    
    @Column(nullable = false)
    @Positive(message = "El año debe ser un número válido")
    @NotNull(message = "El año de fundación es necesario")
    private Integer anñoFundacion;
    
    @Column(nullable = false)
    private String logoUrl;

    // @OneToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name="entrenador_id")

    // private Entrenador entrenador;

} 
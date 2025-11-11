package eafit.caba_pro.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Integer fundacion;
    
    @Column(nullable = false)
    private String logo;

    @OneToMany(mappedBy = "equipoLocal", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Partido> partidosLocal;

    @OneToMany(mappedBy = "equipoVisitante", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Partido> partidosVisitante;

    // Relación con Entrenadores (OneToMany)
    @OneToMany(mappedBy = "equipoAsociado", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Entrenador> entrenadores;

    public List<Entrenador> getEntrenadores() {
        return entrenadores;
    }

    public void setEntrenadores(List<Entrenador> entrenadores) {
        this.entrenadores = entrenadores;
    }
}
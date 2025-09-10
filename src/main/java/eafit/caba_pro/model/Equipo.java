package eafit.caba_pro.model;

import java.util.List;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
    private Integer fundacion;
    
    @Column(nullable = false)
    private String logo;

    @OneToMany(mappedBy = "equipoLocal",fetch = FetchType.LAZY)
    //@JsonManagedReference("local")
    @JsonIgnore
    private List<Partido> partidosLocal;

    @OneToMany(mappedBy = "equipoVisitante",fetch = FetchType.LAZY)
    //@JsonManagedReference("visitante")
    @JsonIgnore
    private List<Partido> partidosVisitante;
} 
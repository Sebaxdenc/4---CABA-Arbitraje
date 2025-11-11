package eafit.caba_pro.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "escalafon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "arbitros")  // Excluir arbitros del toString para evitar ciclo infinito
public class Escalafon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Nombre nombre; 

    @Column(nullable = false)
    private BigDecimal honorarioBase; // cuánto gana por partido

    @OneToMany(mappedBy = "escalafon", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference // Manejar serialización de reseñas
    private List<Arbitro> arbitros = new ArrayList<>();

    public enum Nombre {
        INTERNACIONAL,
        LOCAL,
        NACIONAL
    }
}

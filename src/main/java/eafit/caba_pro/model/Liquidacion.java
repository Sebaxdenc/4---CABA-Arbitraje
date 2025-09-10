package eafit.caba_pro.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "liquidacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Liquidacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private YearMonth periodo; // ej: Septiembre 2025

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaGeneracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLiquidacion estado = EstadoLiquidacion.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "arbitro_id", nullable = false)
    @JsonBackReference
    private Arbitro arbitro;

    @OneToMany(mappedBy = "liquidacion")
    @JsonManagedReference
    private List<Partido> partidos;

    @PositiveOrZero
    @Column
    private BigDecimal total; // total a pagar al árbitro
    
    public enum EstadoLiquidacion{
        PENDIENTE, PAGADA
    }
}

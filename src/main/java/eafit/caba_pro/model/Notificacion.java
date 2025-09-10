package eafit.caba_pro.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notificacion {
    // Constructor para crear notificaciones sin destinatario específico
    public Notificacion(String mensaje, TipoDestinatario tipoDestinatario) {
        this.mensaje = mensaje;
        this.tipoDestinatario = tipoDestinatario;
    }

    public Notificacion(String mensaje, TipoDestinatario tipoDestinatario, Arbitro destinatario) {
        this.mensaje = mensaje;
        this.tipoDestinatario = tipoDestinatario;
        this.destinatario = destinatario;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "arbitro_id", nullable = true)
    private Arbitro destinatario;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoDestinatario tipoDestinatario;

    public enum TipoDestinatario {
        ARBITRO,
        ADMIN
    }
}

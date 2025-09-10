package eafit.caba_pro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrenadores")
public class Entrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "\\d{8,12}", message = "La cédula debe tener entre 8 y 12 dígitos")
    @Column(name = "cedula", nullable = false, unique = true, length = 12)
    private String cedula;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
    @Column(name = "telefono", nullable = false, length = 10)
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "El equipo es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre del equipo debe tener entre 2 y 80 caracteres")
    @Column(name = "equipo", nullable = false, length = 80)
    private String equipo;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Categoria categoria;

    @NotNull(message = "Los años de experiencia son obligatorios")
    @Min(value = 0, message = "La experiencia no puede ser negativa")
    @Max(value = 50, message = "La experiencia no puede ser mayor a 50 años")
    @Column(name = "experiencia", nullable = false)
    private Integer experiencia;

    @Size(max = 500, message = "Las especialidades no pueden exceder 500 caracteres")
    @Column(name = "especialidades", length = 500)
    private String especialidades;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con Usuario (para login)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    // Enum para categorías de entrenadores
    public enum Categoria {
        INFANTIL("Infantil"),
        JUVENIL("Juvenil"),
        ADULTO("Adulto"),
        PROFESIONAL("Profesional"),
        FEMENINO("Femenino"),
        MIXTO("Mixto");

        private final String displayName;

        Categoria(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ==================== CONSTRUCTORES ====================

    public Entrenador() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Entrenador(String nombreCompleto, String cedula, String telefono, String email, 
                     String equipo, Categoria categoria, Integer experiencia) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.cedula = cedula;
        this.telefono = telefono;
        this.email = email;
        this.equipo = equipo;
        this.categoria = categoria;
        this.experiencia = experiencia;
    }

    // ==================== GETTERS Y SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(Integer experiencia) {
        this.experiencia = experiencia;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si el entrenador está activo
     */
    public boolean isActivo() {
        return activo != null && activo;
    }

    /**
     * Obtiene el nombre para mostrar (nombre completo del entrenador)
     */
    public String getDisplayName() {
        return nombreCompleto;
    }

    /**
     * Obtiene información del equipo con categoría
     */
    public String getEquipoConCategoria() {
        return equipo + " (" + categoria.getDisplayName() + ")";
    }

    /**
     * Verifica si tiene usuario asociado
     */
    public boolean tieneUsuario() {
        return usuario != null;
    }

    /**
     * Obtiene el username del usuario asociado (si existe)
     */
    public String getUsername() {
        return usuario != null ? usuario.getUsername() : null;
    }

    /**
     * Verifica si tiene especialidades definidas
     */
    public boolean tieneEspecialidades() {
        return especialidades != null && !especialidades.trim().isEmpty();
    }

    /**
     * Obtiene descripción de experiencia
     */
    public String getExperienciaDescripcion() {
        if (experiencia == null) return "No especificado";
        if (experiencia == 0) return "Sin experiencia";
        if (experiencia == 1) return "1 año";
        return experiencia + " años";
    }

    /**
     * Verifica si es un entrenador experimentado (más de 5 años)
     */
    public boolean esExperimentado() {
        return experiencia != null && experiencia > 5;
    }

    /**
     * Obtiene nivel de experiencia como texto
     */
    public String getNivelExperiencia() {
        if (experiencia == null || experiencia == 0) return "Principiante";
        if (experiencia <= 2) return "Novato";
        if (experiencia <= 5) return "Intermedio";
        if (experiencia <= 10) return "Experimentado";
        return "Experto";
    }

    // ==================== MÉTODOS LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // ==================== MÉTODOS OBJECT ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Entrenador that = (Entrenador) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Entrenador{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", cedula='" + cedula + '\'' +
                ", equipo='" + equipo + '\'' +
                ", categoria=" + categoria +
                ", experiencia=" + experiencia +
                ", activo=" + activo +
                '}';
    }
}
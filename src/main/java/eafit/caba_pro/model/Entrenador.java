package eafit.caba_pro.model; 

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
//import java.util.List;

@Entity
@Table(name = "entrenador")
public class Entrenador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Column(name = "apellidos", nullable = false)
    private String apellidos;
    
    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    @Column(name = "documento", unique = true, nullable = false)
    private String documento;
    
    @Email(message = "Debe ser un email válido")
    @NotBlank(message = "El email es obligatorio")
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    @Column(name = "telefono")
    private String telefono;
    
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    @NotBlank(message = "El equipo es obligatorio")
    @Size(max = 100, message = "El nombre del equipo no puede exceder 100 caracteres")
    @Column(name = "equipo", nullable = false)
    private String equipo;
    
    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Column(name = "anos_experiencia")
    private Integer anosExperiencia;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private Categoria categoria;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @Column(name = "observaciones", length = 500)
    private String observaciones;
    
    // Enum para categorías
    public enum Categoria {
        JUVENIL("Juvenil"),
        MAYOR("Mayor"),
        PROFESIONAL("Profesional"),
        MIXTO("Mixto");
        
        private final String displayName;
        
        Categoria(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructores
    public Entrenador() {
    }
    
    public Entrenador(String nombre, String apellidos, String documento, String email) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.documento = documento;
        this.email = email;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getDocumento() {
        return documento;
    }
    
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getEquipo() {
        return equipo;
    }
    
    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }
    
    public Integer getAnosExperiencia() {
        return anosExperiencia;
    }
    
    public void setAnosExperiencia(Integer anosExperiencia) {
        this.anosExperiencia = anosExperiencia;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }
    
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    @Override
    public String toString() {
        return "Coach{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", equipo='" + equipo + '\'' +
                '}';
    }
}
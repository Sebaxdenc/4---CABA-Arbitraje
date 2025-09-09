package eafit.caba_pro.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "arbitros")
@Data // Generate getters and setters for all fields using lombok
@AllArgsConstructor // Generate a contrustuctor with all the fields
@NoArgsConstructor // Generates a constructor with no fields acordding to JPA
public class Arbitro {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "El nombre no puede ser nulo")
    @NotEmpty(message = "El nombre no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "La contraseña no puede ser nula")
    @NotEmpty(message = "La contraseña no puede estar vacía")
    @Column(nullable = false, length = 100)
    private String contraseña;

    @NotNull(message = "El nombre de usuario no puede ser nulo")
    @NotEmpty(message = "El nombre de usuario no puede estar vacío")
    @Column(nullable = false, length = 100, unique = true)
    private String username;
    
    @NotNull(message = "La cédula no puede ser nula")
    @NotEmpty(message = "La cédula no puede estar vacía")
    @Column(nullable = false, length = 20, unique = true)
    private String cedula;
    
    @NotNull(message = "El teléfono no puede ser nulo")
    @NotEmpty(message = "El teléfono no puede estar vacío")
    @Column(nullable = false, length = 20)
    private String phone;

        
    @NotNull(message = "El teléfono no puede ser nulo")
    @NotEmpty(message = "El teléfono no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String speciality;

    @NotNull(message = "La escala no puede ser nula")
    @NotEmpty(message = "La escala no puede estar vacía")
    @Column(nullable = false, length = 50)
    private String scale;
    
    // Pa guardar fotos como blob

    @Lob
    @Column(name = "photo_data", columnDefinition = "LONGBLOB")
    private byte[] photoData;
        
    @Column(name = "photo_content_type", length = 100)
    private String photoContentType;
    
    @Column(name = "photo_filename", length = 255)
    private String photoFilename;

    // Relación uno a uno con Usuario
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = true)
    @JsonIgnore // Evitar serialización circular
    private Usuario usuario;

    // Relación uno a muchos con Partido
    @OneToMany(mappedBy = "arbitro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Manejar serialización de partidos
    private List<Partido> partidos = new ArrayList<>();

    // Relación uno a muchos con Reseña
    @OneToMany(mappedBy = "arbitro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Manejar serialización de reseñas
    private List<Reseña> reseñas = new ArrayList<>();

    
    // MÉTODO HELPER: Verificar si tiene imagen
    public boolean hasPhoto() {
        return photoData != null && photoData.length > 0;
    }
    
    // MÉTODO HELPER: Obtener URL de la imagen
    public String getPhotoUrl() {
        if (hasPhoto()) {
            return "api/arbitros/" + id + "/photo";
        }
        return "https://placehold.co/150x150";
    }
    
    // MÉTODOS HELPER PARA MANEJAR LA RELACIÓN CON PARTIDOS
    
    // Agregar un partido al árbitro
    public void addPartido(Partido partido) {
        partidos.add(partido);
        partido.setArbitro(this);
    }
    
    // Remover un partido del árbitro
    public void removePartido(Partido partido) {
        partidos.remove(partido);
        partido.setArbitro(null);
    }
    
    // Obtener cantidad de partidos asignados
    public int getCantidadPartidos() {
        return partidos.size();
    }
    
    // Verificar si tiene partidos asignados
    public boolean tienePartidos() {
        return !partidos.isEmpty();
    }
    
    // MÉTODOS HELPER PARA MANEJAR LA RELACIÓN CON RESEÑAS
    
    // Agregar una reseña al árbitro
    public void addReseña(Reseña reseña) {
        reseñas.add(reseña);
        reseña.setArbitro(this);
    }
    
    // Remover una reseña del árbitro
    public void removeReseña(Reseña reseña) {
        reseñas.remove(reseña);
        reseña.setArbitro(null);
    }
    
    // Obtener cantidad de reseñas
    public int getCantidadReseñas() {
        return reseñas.size();
    }
    
    // Calcular promedio de puntuación
    public double getPromedioPuntuacion() {
        if (reseñas.isEmpty()) {
            return 0.0;
        }
        double suma = reseñas.stream().mapToInt(Reseña::getPuntuacion).sum();
        return suma / reseñas.size();
    }
    
    // Verificar si tiene reseñas
    public boolean tieneReseñas() {
        return !reseñas.isEmpty();
    }
 
}
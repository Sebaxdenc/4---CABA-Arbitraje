package eafit.caba_pro.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PartidoDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private EquipoDTO equipoLocal;
    private EquipoDTO equipoVisitante;
    
    // Constructor vacío
    public PartidoDTO() {}
    
    // Constructor con parámetros
    public PartidoDTO(Long id, LocalDate fecha, LocalTime hora, String estado, EquipoDTO equipoLocal, EquipoDTO equipoVisitante) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public EquipoDTO getEquipoLocal() {
        return equipoLocal;
    }
    
    public void setEquipoLocal(EquipoDTO equipoLocal) {
        this.equipoLocal = equipoLocal;
    }
    
    public EquipoDTO getEquipoVisitante() {
        return equipoVisitante;
    }
    
    public void setEquipoVisitante(EquipoDTO equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }
    
    // Clase interna para EquipoDTO
    public static class EquipoDTO {
        private Long id;
        private String nombre;
        
        public EquipoDTO() {}
        
        public EquipoDTO(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
        
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
    }
}

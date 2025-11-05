package eafit.caba_pro.dto;
import eafit.caba_pro.model.Arbitro;

public class ArbitroDTO {
    private Long id;
    private String nombre;
    private String cedula;
    private String username;
    private String telefono;
    private String experiencia;

    // Constructors
    public ArbitroDTO() {}

    public ArbitroDTO(Long id, String nombre, String cedula, String username, 
                      String telefono, String experiencia) {
        this.id = id;
        this.nombre = nombre;
        this.cedula = cedula;
        this.username = username;
        this.telefono = telefono;
        this.experiencia = experiencia;
    }

    // Factory method to create DTO from Entity
    public static ArbitroDTO fromEntity(Arbitro arbitro) {
        return new ArbitroDTO(
            arbitro.getId(),
            arbitro.getNombre(),
            arbitro.getCedula(),
            arbitro.getUsername(),
            arbitro.getPhone(),
            arbitro.getSpeciality()
        );
    }

    // Method to convert DTO to Entity
    public Arbitro toEntity() {
        Arbitro arbitro = new Arbitro();
        arbitro.setId(this.id);
        arbitro.setNombre(this.nombre);
        arbitro.setCedula(this.cedula);
        arbitro.setUsername(this.username);
        arbitro.setPhone(this.telefono);
        arbitro.setSpeciality(this.experiencia);
        return arbitro;
    }

    // Getters and Setters
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

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

}

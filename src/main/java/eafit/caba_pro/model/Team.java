package eafit.caba_pro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "team")
@Data // Generate getters and setters for all fields using lombok
@AllArgsConstructor // Generate a contrustuctor with all the fields
@NoArgsConstructor // Generates a constructor with no fields acordding to JPA
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idTournament;

    @Column(nullable = false)
    @NotBlank(message = "The name is mandatory")
    private String name;
    
    @Column(nullable = false)
    private boolean state;
    
    @Column(nullable = false)
    @NotBlank(message = "The city is necesary")
    @Size(min = 2, max = 100, message = "The name must be between 2 and 100 characters")
    private String city;
    
    @Column(nullable = false)
    @Positive(message = "Year must be a valid number")
    @NotNull(message = "The foundation year is mandatory")
    private Integer foundYear;
    
    @Column(nullable = false)
    private String logo;
}
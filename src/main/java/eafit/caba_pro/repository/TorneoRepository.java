package eafit.caba_pro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eafit.caba_pro.model.Torneo;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    Optional<Torneo> findByNombreIgnoreCase(String nombre);

}

package eafit.caba_pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eafit.caba_pro.model.Equipo;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo,Long> {
}


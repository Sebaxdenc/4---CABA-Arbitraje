package eafit.caba_pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eafit.caba_pro.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
}


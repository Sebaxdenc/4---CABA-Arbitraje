package eafit.caba_pro.repository;

import eafit.caba_pro.model.Escalafon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalafonRepository extends JpaRepository<Escalafon, Long> {
}

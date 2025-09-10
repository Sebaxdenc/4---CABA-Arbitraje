package eafit.caba_pro.repository;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Notificacion;
import eafit.caba_pro.model.Notificacion.TipoDestinatario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByTipoDestinatario(TipoDestinatario tipoDestinatario);

    List<Notificacion> findByDestinatarioId(Long id);

    //List<Notificacion> findByArbitroId(Long arbitroId);
}

package eafit.caba_pro.service;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Notificacion;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.repository.NotificacionRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public void notificarAdmin(String mensaje) {
        notificacionRepository.save(new Notificacion(mensaje, Notificacion.TipoDestinatario.ADMIN));
    }

    // Notificar a un árbitro (un usuario específico)
    public void notificarArbitro(String mensaje, Arbitro arbitro) {
        Notificacion n = new Notificacion(mensaje, Notificacion.TipoDestinatario.ARBITRO, arbitro);
        notificacionRepository.save(n);
    }

    // Notificar a todos los admins
    public void notificarAdmins(String mensaje) {
        Notificacion n = new Notificacion(mensaje, Notificacion.TipoDestinatario.ADMIN);
        notificacionRepository.save(n);
    }

    // Consultar notificaciones para admins
    public List<Notificacion> obtenerNotificacionesAdmin() {
        return notificacionRepository.findByTipoDestinatario(Notificacion.TipoDestinatario.ADMIN);
    }

    // Consultar notificaciones para un árbitro
    public List<Notificacion> obtenerNotificacionesArbitro(Long id) {
        return notificacionRepository.findByDestinatarioId(id);
    }
}
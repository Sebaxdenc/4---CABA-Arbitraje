package eafit.caba_pro.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Torneo;
import eafit.caba_pro.repository.PartidoRepository;
import eafit.caba_pro.repository.TorneoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class TorneoService {

    private final TorneoRepository torneoRepo;
    private final PartidoRepository partidoRepo;


    public TorneoService(TorneoRepository torneoRepo, PartidoRepository partidoRepo) {
        this.torneoRepo = torneoRepo;
        this.partidoRepo = partidoRepo;
    }

    // ------------------- CRUD -------------------
    @Transactional(readOnly = true)
    public List<Torneo> findAll() {
        return torneoRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Torneo> findById(Long id) {
        return torneoRepo.findById(id);
    }

    // Save lo dejamos SOLO para actualizaciones simples vía update()
    // (si lo usas para crear, dará la excepción y estarás obligado a usar crearConPartidos)
    public Torneo save(Torneo torneo) {
        if (torneo.getId() == null) {
            throw new IllegalArgumentException(
                "Para crear un torneo debes asignar al menos un partido. Usa crearConPartidos(torneo, partidoIds)."
            );
        }
        return torneoRepo.save(torneo);
    }

    public Torneo update(Long id, Torneo cambios) {
        Torneo t = torneoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Torneo no encontrado"));
        t.setNombre(cambios.getNombre());
        t.setSede(cambios.getSede());
        t.setFechaInicio(cambios.getFechaInicio());
        t.setFechaFin(cambios.getFechaFin());
        return t; 
    }

    public void delete(Long id) {
        Torneo t = torneoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Torneo no encontrado"));

    
        // Desasignar partidos
        partidoRepo.unassignByTorneoId(id);
        torneoRepo.delete(t);
    }

    // ------------- Crear con PARTIDOS PROGRAMADOS -------------
    public Torneo crearConPartidos(Torneo torneo, List<Long> partidoIds) {
        if (partidoIds == null || partidoIds.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar al menos un partido para crear el torneo.");
        }

        // 1) Crear torneo
        Torneo t = torneoRepo.save(torneo);

        // 2) Traer partidos candidatos
        List<Partido> partidos = partidoRepo.findAllById(partidoIds);
        if (partidos.isEmpty()) {
            throw new IllegalArgumentException("Los partidos seleccionados no existen.");
        }

        // 3) Validaciones
        for (Partido p : partidos) {
            if (p.getTorneo() != null) {
                throw new IllegalArgumentException("El partido ID " + p.getId() + " ya pertenece a un torneo.");
            }
            if (p.getEstado() != Partido.EstadoPartido.PROGRAMADO) {
                throw new IllegalArgumentException("Solo puedes agregar partidos PROGRAMADOS. El partido ID " + p.getId() + " no lo está.");
            }
            // (Opcional) fechas dentro del rango del torneo:
            // if (t.getFechaInicio()!=null && p.getFecha()!=null && p.getFecha().isBefore(t.getFechaInicio())) ...
        }

        // 4) Asignar y guardar
        for (Partido p : partidos) {
            p.setTorneo(t);
        }
        partidoRepo.saveAll(partidos);

        return t;
    }

    // ------------- Relación Torneo-Partido -------------
    public void asignarPartido(Long torneoId, Long partidoId) {
        Torneo torneo = torneoRepo.findById(torneoId)
                .orElseThrow(() -> new EntityNotFoundException("Torneo no encontrado"));
        Partido partido = partidoRepo.findById(partidoId)
                .orElseThrow(() -> new EntityNotFoundException("Partido no encontrado"));

       
        if (partido.getTorneo() != null && torneoId.equals(partido.getTorneo().getId())) return;

       
        if (partido.getTorneo() != null && !torneoId.equals(partido.getTorneo().getId())) {
            throw new IllegalArgumentException("El partido " + partidoId + " ya pertenece a otro torneo.");
        }

    
        if (partido.getEstado() != Partido.EstadoPartido.PROGRAMADO) {
            throw new IllegalArgumentException("Solo puedes asignar partidos en estado PROGRAMADO.");
        }

        partido.setTorneo(torneo);
        partidoRepo.save(partido);
    }

    public void quitarPartido(Long torneoId, Long partidoId) {
        Partido partido = partidoRepo.findById(partidoId)
                .orElseThrow(() -> new EntityNotFoundException("Partido no encontrado"));

        if (partido.getTorneo() != null && partido.getTorneo().getId().equals(torneoId)) {

            partido.setTorneo(null);
            partidoRepo.save(partido);
        }
    }

    @Transactional(readOnly = true)
    public long contarPartidosDeTorneo(Long torneoId) {
        return partidoRepo.countByTorneo_Id(torneoId);
    }

    // ------------- Utilidades para controlador/vistas -------------
    @Transactional(readOnly = true)
    public List<Partido> listarPartidosSinTorneoProgramados() {
        return partidoRepo.findByTorneoIsNullAndEstado(Partido.EstadoPartido.PROGRAMADO);
    }

    @Transactional(readOnly = true)
    public List<Partido> listarPartidosDeTorneo(Long torneoId) {
        torneoRepo.findById(torneoId)
                .orElseThrow(() -> new EntityNotFoundException("Torneo no encontrado"));
        return partidoRepo.findByTorneo_Id(torneoId);
    }
 
}

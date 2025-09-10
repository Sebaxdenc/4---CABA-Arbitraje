package eafit.caba_pro.service;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Liquidacion;
import eafit.caba_pro.model.Notificacion;
import eafit.caba_pro.model.Liquidacion.EstadoLiquidacion;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.repository.ArbitroRepository;
import eafit.caba_pro.repository.LiquidacionRepository;
import eafit.caba_pro.repository.NotificacionRepository;
import eafit.caba_pro.repository.PartidoRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class LiquidacionService {
    private final LiquidacionRepository liquidacionRepository;
    private final ArbitroRepository arbitroRepository;
    private final PartidoRepository partidoRepository ;
    private final NotificacionRepository notificacionRepository;

    public LiquidacionService(NotificacionRepository notificacionRepository,PartidoRepository partidoRepository, ArbitroRepository arbitroRepository,LiquidacionRepository liquidacionRepository){
        this.liquidacionRepository = liquidacionRepository;
        this.arbitroRepository = arbitroRepository;
        this.partidoRepository = partidoRepository;
        this.notificacionRepository = notificacionRepository;
    }    

    public Optional<Liquidacion> findById(Long id) {
        return liquidacionRepository.findById(id);
    }

    @Transactional
    public void generarLiquidacionesMensuales(YearMonth periodo) {
        LocalDate inicioMes = periodo.atDay(1);
        LocalDate finMes = periodo.atEndOfMonth();

        List<Arbitro> arbitros = arbitroRepository.findAll();

        for (Arbitro arbitro : arbitros) {
            if (liquidacionRepository.existsByArbitroIdAndPeriodo(arbitro.getId(), periodo)) {
                continue;
            }

            List<Partido> partidosPendientes = partidoRepository
                    .findByArbitroAndFechaBetweenAndLiquidacionIsNull(arbitro, inicioMes, finMes);

            if (partidosPendientes.isEmpty()) continue;

            Liquidacion liquidacion = new Liquidacion();
            liquidacion.setArbitro(arbitro);
            liquidacion.setPeriodo(periodo);
            liquidacion.setFechaGeneracion(LocalDate.now());
            liquidacion.setEstado(EstadoLiquidacion.PENDIENTE);

            BigDecimal total = BigDecimal.ZERO;
            for (Partido partido : partidosPendientes) {
                total = total.add(partido.getArbitro().getEscalafon().getHonorarioBase());
            }

            liquidacion.setTotal(total);
            liquidacionRepository.save(liquidacion);

            Notificacion notificacion = new Notificacion(
                    "Se ha generado una nueva liquidación para el periodo " + periodo + ". Total: $" + total,
                    Notificacion.TipoDestinatario.ARBITRO, arbitro);
            notificacionRepository.save(notificacion);

            for (Partido partido : partidosPendientes) {
                partido.setLiquidacion(liquidacion);
                partidoRepository.save(partido);
            }
        }
    }

    /**
     * Marcar una liquidación como pagada.
     */
    @Transactional
    public void marcarComoPagada(Long liquidacionId) {
        Liquidacion liquidacion = liquidacionRepository.findById(liquidacionId)
                .orElseThrow(() -> new EntityNotFoundException("Liquidación no encontrada"));

        liquidacion.setEstado(EstadoLiquidacion.PAGADA);
        liquidacionRepository.save(liquidacion);
    }

    /**
     * Consultar liquidaciones por mes y año.
     */
    public List<Liquidacion> obtenerLiquidacionesPorPeriodo(YearMonth yearMonth) {
        return liquidacionRepository.findByPeriodo(yearMonth);
    }

    /**
     * Consultar liquidaciones de un árbitro en un periodo.
     */
    public List<Liquidacion> obtenerLiquidacionesPorArbitroYPeriodo(Long arbitroId, YearMonth yearMonth) {
        return liquidacionRepository.findByArbitroIdAndPeriodo(arbitroId, yearMonth);
    }

    /**
     * Consultar todas las liquidaciones de un árbitro.
     */
    public List<Liquidacion> obtenerLiquidacionesPorArbitro(Long arbitroId) {
        return liquidacionRepository.findByArbitroId(arbitroId);
    }

    public Liquidacion obtenerPorId(Long id) {
        return liquidacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Liquidación no encontrada con ID: " + id));
    }

    /**
     * Consultar todas las liquidaciones pendientes.
     */
    public List<Liquidacion> obtenerPendientes() {
        return liquidacionRepository.findByEstado(EstadoLiquidacion.PENDIENTE);
    }
}

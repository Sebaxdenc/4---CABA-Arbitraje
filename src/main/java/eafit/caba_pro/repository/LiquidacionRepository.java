package eafit.caba_pro.repository;

import java.time.YearMonth;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eafit.caba_pro.model.Liquidacion;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    List<Liquidacion> findByArbitroIdAndPeriodo(Long arbitroId, YearMonth periodo);

    List<Liquidacion> findByPeriodo(YearMonth periodo);

    boolean existsByArbitroIdAndPeriodo(Long arbitroId, YearMonth periodo);

    List<Liquidacion> findByEstado(Liquidacion.EstadoLiquidacion estado);

    List<Liquidacion> findByArbitroIdAndEstado(Long arbitroId, Liquidacion.EstadoLiquidacion estado);

    List<Liquidacion> findByArbitroId(Long arbitroId);
}

package eafit.caba_pro.repository;

import eafit.caba_pro.model.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntrenadorRepository extends JpaRepository<Entrenador, Long> {

    // ==================== BÚSQUEDAS BÁSICAS ====================
    
    /**
     * Encontrar todos los entrenadores activos
     */
    List<Entrenador> findByActivoTrue();

    /**
     * Encontrar entrenador por ID y activo
     */
    Optional<Entrenador> findByIdAndActivoTrue(Long id);

    // ==================== BÚSQUEDAS POR CAMPOS ESPECÍFICOS ====================

    /**
     * Buscar por nombre completo (ignora mayúsculas/minúsculas)
     */
    List<Entrenador> findByNombreCompletoContainingIgnoreCaseAndActivoTrue(String nombreCompleto);

    /**
     * Buscar por equipo (ignora mayúsculas/minúsculas)
     */
    List<Entrenador> findByEquipoContainingIgnoreCaseAndActivoTrue(String equipo);

    /**
     * Buscar por categoría
     */
    List<Entrenador> findByCategoriaAndActivoTrue(Entrenador.Categoria categoria);

    /**
     * Buscar por cédula
     */
    Optional<Entrenador> findByCedulaAndActivoTrue(String cedula);

    /**
     * Buscar por email
     */
    Optional<Entrenador> findByEmailAndActivoTrue(String email);

    /**
     * Buscar por username del usuario asociado
     */
    Optional<Entrenador> findByUsuario_UsernameAndActivoTrue(String username);

    /**
     * Buscar por ID del usuario asociado
     */
    Optional<Entrenador> findByUsuario_IdAndActivoTrue(Long usuarioId);

    // ==================== VALIDACIONES DE EXISTENCIA ====================

    /**
     * Verificar si existe por cédula
     */
    boolean existsByCedulaAndActivoTrue(String cedula);

    /**
     * Verificar si existe por email
     */
    boolean existsByEmailAndActivoTrue(String email);

    /**
     * Verificar si existe por username del usuario
     */
    boolean existsByUsuario_UsernameAndActivoTrue(String username);

    /**
     * Verificar si existe por equipo
     */
    boolean existsByEquipoAndActivoTrue(String equipo);

    // ==================== CONTEOS ====================

    /**
     * Contar entrenadores activos
     */
    long countByActivoTrue();

    /**
     * Contar por categoría
     */
    long countByCategoriaAndActivoTrue(Entrenador.Categoria categoria);

    /**
     * Contar por equipo
     */
    long countByEquipoAndActivoTrue(String equipo);

    // ==================== BÚSQUEDAS ORDENADAS ====================

    /**
     * Encontrar entrenadores ordenados por nombre
     */
    List<Entrenador> findByActivoTrueOrderByNombreCompletoAsc();

    /**
     * Encontrar entrenadores ordenados por fecha de creación (más recientes primero)
     */
    List<Entrenador> findByActivoTrueOrderByFechaCreacionDesc();

    /**
     * Encontrar entrenadores ordenados por experiencia (descendente)
     */
    List<Entrenador> findByActivoTrueOrderByExperienciaDesc();

    /**
     * Encontrar entrenadores ordenados por equipo
     */
    List<Entrenador> findByActivoTrueOrderByEquipoAsc();

    // ==================== BÚSQUEDAS LIMITADAS (TOP N) ====================

    /**
     * Encontrar top 5 entrenadores por categoría
     */
    List<Entrenador> findTop5ByCategoriaAndActivoTrueOrderByFechaCreacionDesc(Entrenador.Categoria categoria);

    /**
     * Encontrar top 10 entrenadores más recientes
     */
    List<Entrenador> findTop10ByActivoTrueOrderByFechaCreacionDesc();

    /**
     * Encontrar top entrenadores con más experiencia
     */
    List<Entrenador> findTop5ByActivoTrueOrderByExperienciaDesc();

    // ==================== BÚSQUEDAS POR FECHAS ====================

    /**
     * Encontrar entrenadores creados después de una fecha
     */
    List<Entrenador> findByActivoTrueAndFechaCreacionAfterOrderByFechaCreacionDesc(LocalDateTime fecha);

    /**
     * Encontrar entrenadores creados entre fechas
     */
    List<Entrenador> findByActivoTrueAndFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Encontrar entrenadores actualizados recientemente
     */
    List<Entrenador> findByActivoTrueAndFechaActualizacionAfter(LocalDateTime fecha);

    // ==================== CONSULTAS PERSONALIZADAS ====================

    /**
     * Buscar entrenadores por múltiples criterios usando JPQL
     */
    @Query("SELECT e FROM Entrenador e WHERE e.activo = true " +
           "AND (:nombre IS NULL OR UPPER(e.nombreCompleto) LIKE UPPER(CONCAT('%', :nombre, '%'))) " +
           "AND (:equipo IS NULL OR UPPER(e.equipo) LIKE UPPER(CONCAT('%', :equipo, '%'))) " +
           "AND (:categoria IS NULL OR e.categoria = :categoria)")
    List<Entrenador> findByMultipleCriteria(@Param("nombre") String nombre, 
                                           @Param("equipo") String equipo, 
                                           @Param("categoria") Entrenador.Categoria categoria);

    /**
     * Obtener estadísticas básicas de entrenadores por equipo
     */
    @Query("SELECT e.equipo, COUNT(e) FROM Entrenador e WHERE e.activo = true GROUP BY e.equipo")
    List<Object[]> countEntrenadoresPorEquipo();

    /**
     * Obtener estadísticas básicas por categoría
     */
    @Query("SELECT e.categoria, COUNT(e) FROM Entrenador e WHERE e.activo = true GROUP BY e.categoria")
    List<Object[]> countEntrenadoresPorCategoria();

    /**
     * Encontrar entrenadores sin usuario asociado
     */
    @Query("SELECT e FROM Entrenador e WHERE e.activo = true AND e.usuario IS NULL")
    List<Entrenador> findEntrenadoresSinUsuario();

    /**
     * Encontrar entrenadores con usuario pero sin rol específico
     */
    @Query("SELECT e FROM Entrenador e WHERE e.activo = true AND e.usuario IS NOT NULL " +
           "AND (e.usuario.role IS NULL OR e.usuario.role != 'COACH')")
    List<Entrenador> findEntrenadoresConUsuarioSinRolCoach();

    /**
     * Buscar entrenadores por experiencia mínima
     */
    List<Entrenador> findByActivoTrueAndExperienciaGreaterThanEqual(Integer experienciaMinima);

    /**
     * Buscar entrenadores por rango de experiencia
     */
    List<Entrenador> findByActivoTrueAndExperienciaBetween(Integer experienciaMin, Integer experienciaMax);

    /**
     * Encontrar entrenadores que contengan ciertas especialidades
     */
    @Query("SELECT e FROM Entrenador e WHERE e.activo = true " +
           "AND (:especialidad IS NULL OR UPPER(e.especialidades) LIKE UPPER(CONCAT('%', :especialidad, '%')))")
    List<Entrenador> findByEspecialidadContaining(@Param("especialidad") String especialidad);

    /**
     * Búsqueda general (nombre, equipo, email)
     */
    @Query("SELECT e FROM Entrenador e WHERE e.activo = true AND " +
           "(UPPER(e.nombreCompleto) LIKE UPPER(CONCAT('%', :termino, '%')) OR " +
           "UPPER(e.equipo) LIKE UPPER(CONCAT('%', :termino, '%')) OR " +
           "UPPER(e.email) LIKE UPPER(CONCAT('%', :termino, '%')))")
    List<Entrenador> busquedaGeneral(@Param("termino") String termino);

    // ==================== OPERACIONES DE SOFT DELETE ====================

    /**
     * Encontrar entrenadores eliminados (soft delete)
     */
    List<Entrenador> findByActivoFalse();

    /**
     * Encontrar todos incluyendo eliminados
     */
    @Query("SELECT e FROM Entrenador e ORDER BY e.activo DESC, e.fechaCreacion DESC")
    List<Entrenador> findAllIncludingDeleted();
}
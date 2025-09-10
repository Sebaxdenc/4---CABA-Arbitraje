package eafit.caba_pro.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.repository.ArbitroRepository;
import eafit.caba_pro.repository.PartidoRepository;
import eafit.caba_pro.repository.UsuarioRepository;


@Service
@Transactional(readOnly = true)
public class ArbitroService {

    private final ArbitroRepository arbitroRepository;
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    // Constructor para inyección de dependencias
    public ArbitroService(NotificacionService notificacionService, ArbitroRepository arbitroRepository, PartidoRepository partidoRepository,UsuarioRepository usuarioRepository,UsuarioService usuarioService) {
        this.arbitroRepository = arbitroRepository;
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    // ========== OPERACIONES DE LECTURA ==========

    /**
     * Buscar árbitro por cédula
     */
    public Optional<Arbitro> findByCedula(String cedula) {
        return arbitroRepository.findByCedula(cedula);
    }

    /**
     * Buscar árbitro por nombre de usuario
     */
    public Optional<Arbitro> findByUsername(String username) {
        return arbitroRepository.findByUsername(username);
    }


    /**
     * Obtener todos los árbitros
     */
    public List<Arbitro> findAll() {
        return arbitroRepository.findAll();
    }

    /**
     * Obtener árbitro por ID
     */
    public Optional<Arbitro> findById(Long id) {
        return arbitroRepository.findById(id);
    }

    /**
     * Verificar si existe un árbitro con la cédula dada
     */
    public boolean existsByCedula(String cedula) {
        return arbitroRepository.existsByCedula(cedula);
    }

    /**
     * Verificar si existe un árbitro con el teléfono dado
     */
    public boolean existsByPhone(String phone) {
        return arbitroRepository.existsByPhone(phone);
    }

    // ========== OPERACIONES DE ESCRITURA ==========

    /**
     * Crear árbitro con archivo de foto (guarda BLOB en BD)
     */

    @Transactional
    public Arbitro createArbitroWithPhoto(Arbitro arbitro, MultipartFile photoFile) throws IOException {
        // Validaciones de duplicados

        if (arbitro.getCedula() != null && arbitroRepository.existsByCedula(arbitro.getCedula())) {
            throw new RuntimeException("Esta cédula ya está registrada: " + arbitro.getCedula());
        }
        
        if (arbitro.getPhone() != null && arbitroRepository.existsByPhone(arbitro.getPhone())) {
            throw new RuntimeException("Este teléfono ya está registrado: " + arbitro.getPhone());
        }
        
        
        Usuario usuario = arbitro.getUsuario();
            if (usuario == null) {
                throw new RuntimeException("Debes asociar un usuario al árbitro");
         }

        arbitro.setUsername(usuario.getUsername());

        usuario.setRole("ROLE_ARBITRO");

        usuarioRepository.save(usuario);

        // Procesar imagen si se proporciona
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                // Validar tipo de archivo
                String contentType = photoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("El archivo debe ser una imagen válida (JPG, PNG, GIF)");
                }

                // Validar tamaño (máximo 5MB)
                if (photoFile.getSize() > 5 * 1024 * 1024) {
                    throw new RuntimeException("La imagen no puede ser mayor a 5MB");
                }

                // Guardar datos de la imagen
                arbitro.setPhotoData(photoFile.getBytes());
                arbitro.setPhotoContentType(contentType);
                arbitro.setPhotoFilename(photoFile.getOriginalFilename());

                System.out.println("Imagen procesada: " + photoFile.getOriginalFilename() + 
                                 " (" + photoFile.getSize() + " bytes)");

            } catch (IOException e) {
                System.err.println("Error al procesar imagen: " + e.getMessage());
                throw new IOException("Error al procesar la imagen", e);
            }
        } else {
            System.out.println("No se proporcionó imagen para el árbitro");
        }

        return arbitroRepository.save(arbitro);
    }

    /**
     * Actualizar árbitro con archivo de foto (guarda BLOB en BD)
     */
    @Transactional
    public Arbitro updateArbitroWithPhoto(Arbitro arbitro, MultipartFile photoFile, boolean removePhoto, boolean updatePassword) throws IOException {
        // Buscar el árbitro existente
        Arbitro existingArbitro = arbitroRepository.findById(arbitro.getId())
            .orElseThrow(() -> new RuntimeException("Árbitro no encontrado"));

        // Validaciones de duplicados (excluyendo el árbitro actual)
        if (arbitro.getCedula() != null && !arbitro.getCedula().equals(existingArbitro.getCedula()) 
            && arbitroRepository.existsByCedula(arbitro.getCedula())) {
            throw new RuntimeException("Esta cédula ya está registrada: " + arbitro.getCedula());
        }
        
        if (arbitro.getPhone() != null && !arbitro.getPhone().equals(existingArbitro.getPhone()) 
            && arbitroRepository.existsByPhone(arbitro.getPhone())) {
            throw new RuntimeException("Este teléfono ya está registrado: " + arbitro.getPhone());
        }

        // Actualizar campos básicos
        existingArbitro.setNombre(arbitro.getNombre());
        existingArbitro.setCedula(arbitro.getCedula());
        existingArbitro.setPhone(arbitro.getPhone());
        existingArbitro.setSpeciality(arbitro.getSpeciality());
        existingArbitro.setEscalafon(arbitro.getEscalafon());

        // Manejar usuario
        Usuario usuario = arbitro.getUsuario();
        if (usuario != null) {
            Usuario existingUsuario = existingArbitro.getUsuario();
            if (existingUsuario != null) {
                existingUsuario.setUsername(usuario.getUsername());
                
                // Solo actualizar contraseña si se especifica
                if (updatePassword && usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    // Agregar prefijo {noop} si no está presente
                    String password = usuario.getPassword();
                    if (!password.startsWith("{")) {
                        password = "{noop}" + password;
                    }
                    existingUsuario.setPassword(password);
                    usuarioRepository.save(existingUsuario);
                }
            }
            existingArbitro.setUsername(usuario.getUsername());
        }

        // Manejar imagen
        if (removePhoto) {
            // Remover foto actual
            existingArbitro.setPhotoData(null);
            existingArbitro.setPhotoContentType(null);
            existingArbitro.setPhotoFilename(null);
            System.out.println("Foto removida para el árbitro: " + existingArbitro.getNombre());
        } else if (photoFile != null && !photoFile.isEmpty()) {
            // Actualizar con nueva foto
            try {
                // Validar tipo de archivo
                String contentType = photoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("El archivo debe ser una imagen válida (JPG, PNG, GIF)");
                }

                // Validar tamaño (máximo 5MB)
                if (photoFile.getSize() > 5 * 1024 * 1024) {
                    throw new RuntimeException("La imagen no puede ser mayor a 5MB");
                }

                // Guardar datos de la nueva imagen
                existingArbitro.setPhotoData(photoFile.getBytes());
                existingArbitro.setPhotoContentType(contentType);
                existingArbitro.setPhotoFilename(photoFile.getOriginalFilename());

                System.out.println("Nueva imagen procesada: " + photoFile.getOriginalFilename() + 
                                 " (" + photoFile.getSize() + " bytes)");

            } catch (IOException e) {
                System.err.println("Error al procesar imagen: " + e.getMessage());
                throw new IOException("Error al procesar la imagen", e);
            }
        }
        // Si no se especifica removePhoto ni se proporciona nueva foto, mantener la foto actual

        return arbitroRepository.save(existingArbitro);
    }

    /**
     * Verificar si un árbitro puede ser eliminado (no tiene partidos asignados)
     */
    public boolean canDelete(Long arbitroId) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        if (!arbitroOpt.isPresent()) {
            return false;
        }
        
        // Verificar si tiene partidos asignados
        return partidoRepository.findByArbitro(arbitroOpt.get()).isEmpty();
    }

    /**
     * Eliminar árbitro por ID
     */
    @Transactional
    public boolean deleteById(Long id) {
        try {
            Optional<Arbitro> arbitroOptional = arbitroRepository.findById(id);
            
            if (arbitroOptional.isPresent()) {
                Arbitro arbitro = arbitroOptional.get();
                
                // Verificar si tiene partidos asignados
                if (!partidoRepository.findByArbitro(arbitro).isEmpty()) {
                    throw new RuntimeException("No se puede eliminar el árbitro '" + arbitro.getNombre() + 
                                             "' porque tiene partidos asignados. Primero debe reasignar o eliminar los partidos.");
                }
                
                // Eliminar de la base de datos (la imagen BLOB se elimina automáticamente)
                arbitroRepository.deleteById(id);
                System.out.println("Árbitro eliminado completamente: " + arbitro.getNombre());
                
                return true;
            } else {
                System.out.println("Árbitro no encontrado con ID: " + id);
                return false;
            }
        } catch (RuntimeException e) {
            // Re-lanzar errores de negocio
            throw e;
        } catch (Exception e) {
            System.err.println("Error al eliminar árbitro con ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Error interno al eliminar árbitro: " + e.getMessage(), e);
        }
    }
    
    // MÉTODOS NUEVOS PARA APROVECHAR LA RELACIÓN BIDIRECCIONAL
    
    /**
     * Obtener partidos de un árbitro usando la relación bidireccional
     */
    public List<Partido> getPartidosDeArbitro(Long arbitroId) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        return arbitroOpt.map(Arbitro::getPartidos).orElse(List.of());
    }
    
    /**
     * Asignar un partido a un árbitro usando los métodos helper
     */
    @Transactional
    public boolean asignarPartido(Long arbitroId, Partido partido) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            arbitro.addPartido(partido); // Usa el método helper que mantiene sincronización
            arbitroRepository.save(arbitro);
            return true;
        }
        return false;
    }
    
    /**
     * Desasignar un partido de un árbitro
     */
    @Transactional
    public boolean desasignarPartido(Long arbitroId, Partido partido) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            arbitro.removePartido(partido); // Usa el método helper
            arbitroRepository.save(arbitro);
            return true;
        }
        return false;
    }
    
    /**
     * Verificar si un árbitro está disponible en una fecha/hora específica
     */
    public boolean isArbitroDisponible(Long arbitroId, LocalDate fecha, LocalTime hora) {
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        if (arbitroOpt.isPresent()) {
            Arbitro arbitro = arbitroOpt.get();
            return arbitro.getPartidos().stream()
                .noneMatch(p -> p.getFecha().equals(fecha) && p.getHora().equals(hora));
        }
        return false;
    }
    
    /**
     * Obtener árbitros disponibles en una fecha/hora específica
     */
    public List<Arbitro> getArbitrosDisponibles(LocalDate fecha, LocalTime hora) {
        return arbitroRepository.findAvailableArbitrosAt(fecha, hora);
    }
    
    /**
     * Obtener árbitros sin partidos asignados
     */
    public List<Arbitro> getArbitrosSinPartidos() {
        return arbitroRepository.findArbitrosSinPartidos();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> buildPhotoResponse(Long id) {
        Optional<Arbitro> opt = arbitroRepository.findById(id);
        if (opt.isEmpty() || !opt.get().hasPhoto()) return ResponseEntity.notFound().build();

        Arbitro a = opt.get();
        String ct = (a.getPhotoContentType() != null) ? a.getPhotoContentType() : MediaType.IMAGE_JPEG_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, ct)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(a.getPhotoData());
    }

   public List<Arbitro> findTop5ActivosDelMes() {
    return arbitroRepository.findTop5ActivosDelMes(PageRequest.of(0, 5));
   }
    @Transactional
    public void crearArbitro(Arbitro arbitro) {
        // 1. Crear Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(arbitro.getNombre());
        usuario.setPassword("{noop}" + arbitro.getContraseña());
        usuario.setRole("ROLE_ARBITRO");

        usuarioService.createUsuario(usuario);

        // 2. Asociar usuario al árbitro
        arbitro.setUsuario(usuario);
        arbitroRepository.save(arbitro);

    }

    // ========== MÉTODOS ADICIONALES PARA DISPONIBILIDAD ==========

    /**
     * Obtener todos los árbitros excepto el especificado
     */
    public List<Arbitro> findAllExcept(Long arbitroId) {
        return arbitroRepository.findAll().stream()
                .filter(arbitro -> !arbitro.getId().equals(arbitroId))
                .toList();
    }

    /**
     * Buscar todos los árbitros activos
     */
    public List<Arbitro> findAllActivos() {
        return arbitroRepository.findAll().stream()
                .filter(arbitro -> arbitro.getUsuario() != null && arbitro.getUsuario().isActivo())
                .toList();
    }
    // ========== MÉTODOS DE DISPONIBILIDAD ==========

    /**
     * Confirmar disponibilidad de un árbitro para un partido
     */
    @Transactional
    public String confirmarDisponibilidad(Long partidoId, String username) {
        try {
            Optional<Arbitro> arbitroOpt = findByUsername(username);
            
            if (arbitroOpt.isEmpty()) {
                return "Árbitro no encontrado";
            }

            Arbitro arbitro = arbitroOpt.get();
            Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
            
            if (partidoOpt.isEmpty()) {
                return "Partido no encontrado";
            }

            Partido partido = partidoOpt.get();

            if (!partido.getArbitro().getId().equals(arbitro.getId())) {
                return "No tienes permiso para confirmar este partido";
            }

            notificacionService.notificarArbitro("Disponibilidad confirmada: El árbitro " + arbitro.getNombre() + " ha confirmado su disponibilidad para el partido " +
                                           partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre() +
                                           " programado para el " + partido.getFecha() + " a las " + partido.getHora() + ".", arbitro);
                        

            notificacionService.notificarAdmin("Disponibilidad confirmada: El árbitro " + arbitro.getNombre() + " ha confirmado su disponibilidad para el partido " +
                                           partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre() +
                                           " programado para el " + partido.getFecha() + " a las " + partido.getHora() + ".");
            // Cambiar estado a PROGRAMADO
            partido.setEstado(Partido.EstadoPartido.PROGRAMADO);
            partidoRepository.save(partido);

            return "SUCCESS:Disponibilidad confirmada exitosamente para el partido " + 
                   partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre();

        } catch (Exception e) {
            return "Error al confirmar la disponibilidad: " + e.getMessage();
        }
    }

    /**
     * Marcar árbitro como no disponible para un partido
     */
    @Transactional
    public String marcarNoDisponible(Long partidoId, String username) {
        try {
            Optional<Arbitro> arbitroOpt = findByUsername(username);
            
            if (arbitroOpt.isEmpty()) {
                return "Árbitro no encontrado";
            }

            Arbitro arbitro = arbitroOpt.get();
            Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
            
            if (partidoOpt.isEmpty()) {
                return "Partido no encontrado";
            }

            Partido partido = partidoOpt.get();

            if (!partido.getArbitro().getId().equals(arbitro.getId())) {
                return "No tienes permiso para modificar este partido";
            }

            // Cambiar estado a ARBITRO_NO_DISPONIBLE
            partido.setEstado(Partido.EstadoPartido.ARBITRO_NO_DISPONIBLE);
            partidoRepository.save(partido);

            return "SUCCESS:Has marcado tu no disponibilidad para el partido " + 
                   partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre() + 
                   ". El administrador deberá asignar un nuevo árbitro.";

        } catch (Exception e) {
            return "Error al marcar no disponibilidad: " + e.getMessage();
        }
    }

    /**
     * Reasignar partido a otro árbitro
     */
    @Transactional
    public String reasignarPartido(Long partidoId, Long nuevoArbitroId, String username) {
        try {
            Optional<Arbitro> arbitroActualOpt = findByUsername(username);
            
            if (arbitroActualOpt.isEmpty()) {
                return "Árbitro actual no encontrado";
            }

            Arbitro arbitroActual = arbitroActualOpt.get();
            Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
            Optional<Arbitro> nuevoArbitroOpt = findById(nuevoArbitroId);
            
            if (partidoOpt.isEmpty()) {
                return "Partido no encontrado";
            }

            if (nuevoArbitroOpt.isEmpty()) {
                return "Árbitro seleccionado no encontrado";
            }

            Partido partido = partidoOpt.get();
            Arbitro nuevoArbitro = nuevoArbitroOpt.get();

            if (!partido.getArbitro().getId().equals(arbitroActual.getId())) {
                return "No tienes permiso para modificar este partido";
            }

            // Reasignar el partido al nuevo árbitro
            partido.setArbitro(nuevoArbitro);
            // Mantener el estado como PENDIENTE_CONFIRMACION para que el nuevo árbitro lo confirme
            partidoRepository.save(partido);

            return "SUCCESS:Partido reasignado exitosamente a " + nuevoArbitro.getNombre() + 
                   ". El nuevo árbitro deberá confirmar su disponibilidad.";

        } catch (Exception e) {
            return "Error al reasignar el partido: " + e.getMessage();
        }
    }
}
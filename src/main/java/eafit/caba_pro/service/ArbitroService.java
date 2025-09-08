package eafit.caba_pro.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.repository.ArbitroRepository;
import eafit.caba_pro.repository.PartidoRepository;

@Service
@Transactional(readOnly = true)
public class ArbitroService {

    private final ArbitroRepository arbitroRepository;
    private final PartidoRepository partidoRepository;

    // Constructor para inyección de dependencias
    public ArbitroService(ArbitroRepository arbitroRepository, PartidoRepository partidoRepository) {
        this.arbitroRepository = arbitroRepository;
        this.partidoRepository = partidoRepository;
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
}
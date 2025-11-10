package eafit.caba_pro.service;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.repository.ArbitroRepository;
import eafit.caba_pro.repository.PartidoRepository;
import eafit.caba_pro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArbitroServiceTest {

    private final ArbitroRepository arbitroRepository = Mockito.mock(ArbitroRepository.class);
    private final PartidoRepository partidoRepository = Mockito.mock(PartidoRepository.class);
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
    private final NotificacionService notificacionService = Mockito.mock(NotificacionService.class);

    private ArbitroService service() {
        return new ArbitroService(notificacionService, arbitroRepository, partidoRepository, usuarioRepository, usuarioService);
    }

    @Test
    void canDelete_devuelveTrue_siNoTienePartidos() {
        Arbitro a = new Arbitro();
        a.setId(1L);

        when(arbitroRepository.findById(1L)).thenReturn(Optional.of(a));
        when(partidoRepository.findByArbitro(a)).thenReturn(Collections.emptyList());

        boolean result = service().canDelete(1L);

        assertTrue(result, "Debe permitir borrar cuando no hay partidos asignados");
    }

    @Test
    void canDelete_devuelveFalse_siTienePartidos() {
        Arbitro a = new Arbitro();
        a.setId(2L);

        when(arbitroRepository.findById(2L)).thenReturn(Optional.of(a));
        when(partidoRepository.findByArbitro(a)).thenReturn(List.of(new eafit.caba_pro.model.Partido()));

        boolean result = service().canDelete(2L);

        assertFalse(result, "No debe permitir borrar cuando hay partidos asignados");
    }

    @Test
    void deleteById_lanzaExcepcion_siTienePartidosAsignados() {
        Arbitro a = new Arbitro();
        a.setId(3L);
        a.setNombre("Arbitro Test");

        when(arbitroRepository.findById(3L)).thenReturn(Optional.of(a));
        when(partidoRepository.findByArbitro(a)).thenReturn(List.of(new eafit.caba_pro.model.Partido()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service().deleteById(3L));
        assertTrue(ex.getMessage().contains("No se puede eliminar"));
        verify(arbitroRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_eliminaYDevuelveTrue_siNoTienePartidos() {
        Arbitro a = new Arbitro();
        a.setId(4L);
        a.setNombre("Para Borrar");

        when(arbitroRepository.findById(4L)).thenReturn(Optional.of(a));
        when(partidoRepository.findByArbitro(a)).thenReturn(Collections.emptyList());

        boolean ok = service().deleteById(4L);
        assertTrue(ok);
        verify(arbitroRepository).deleteById(4L);
    }
}

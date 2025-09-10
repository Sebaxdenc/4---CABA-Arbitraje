package eafit.caba_pro.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService implements UserDetailsService{
    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        // QUITAR la llave } que está aquí
        
        Set<GrantedAuthority> grantList = new HashSet<>();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(usuario.getRole());
        grantList.add(grantedAuthority);

        UserDetails user = new User(username, usuario.getPassword(), grantList);

        return user;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        }
        return null;
    }    
    
    public void createUsuario(Usuario usuario){
        usuarioRepository.save(usuario);
    }

    /**
     * Método para corregir passwords existentes que no tienen el prefijo {noop}
     * Se ejecuta al inicializar la aplicación
     */
    @PostConstruct
    @Transactional
    public void fixExistingPasswords() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            boolean hasUpdates = false;
            
            for (Usuario usuario : usuarios) {
                String password = usuario.getPassword();
                if (password != null && !password.startsWith("{")) {
                    // Password sin prefijo, agregar {noop}
                    usuario.setPassword("{noop}" + password);
                    usuarioRepository.save(usuario);
                    hasUpdates = true;
                    System.out.println("Corrigiendo password para usuario: " + usuario.getUsername());
                }
            }
            
            if (hasUpdates) {
                System.out.println("✓ Passwords corregidos exitosamente con prefijo {noop}");
            } else {
                System.out.println("✓ Todos los passwords ya tienen el formato correcto");
            }
            
        } catch (Exception e) {
            System.err.println("Error al corregir passwords existentes: " + e.getMessage());
        }
    }
}
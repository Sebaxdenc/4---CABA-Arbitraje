package eafit.caba_pro.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import eafit.caba_pro.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
    Usuario findByUsername(String username);
    boolean existsByUsername(String username);
}
 
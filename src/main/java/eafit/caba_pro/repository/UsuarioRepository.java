package eafit.caba_pro.repository;
import eafit.caba_pro.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
    Usuario findByUsername(String username);
}
 
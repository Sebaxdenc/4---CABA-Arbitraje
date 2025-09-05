package eafit.caba_pro.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccesHandler extends SavedRequestAwareAuthenticationSuccessHandler{

    private final Map<String, String> roleUrls = Map.of(
        "ROLE_ADMIN", "/admin",
        "ROLE_ENTRENADOR", "/entrenador",
        "ROLE_ARBITRO", "/arbitro"
        );

    @Override   
    public void onAuthenticationSuccess(HttpServletRequest request,HttpServletResponse response, Authentication authentication)
                                            throws IOException,ServletException{
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String url = roleUrls.get(authority.getAuthority());
            if (url != null) {
                response.sendRedirect(url);
                return;
            }
        }
        response.sendRedirect("/");
    }
}
package eafit.caba_pro.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    LoginSuccesHandler loginSuccesHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/","/h2-consola/**","/login","/css/**","/js/**","/images/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/entrenador/**").hasRole("ENTRENADOR")
            .requestMatchers("/arbitro/**").hasRole("ARBITRO")
            .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .maximumSessions(100)
                .maxSessionsPreventsLogin(true)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccesHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf((csrf) -> csrf.disable()) // Nota: en producción, mantener CSRF habilitado y configurar adecuadamente.
            // Permitir frames (necesario para la consola H2)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));;
        return http.build();
    }

    //@Bean
    //public PasswordEncoder passwordEncoder() {
    //    return new BCryptPasswordEncoder();
    //}


}

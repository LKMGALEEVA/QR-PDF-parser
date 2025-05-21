package aseproject.qrpdfparser.config;

import aseproject.qrpdfparser.config.security.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
//        UserDetails admin = User.builder().username("admin").password(encoder.encode("admin")).build();
//        UserDetails user = User.builder().username("user").password(encoder.encode("user")).build();
        return new AppUserDetailsService();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth.requestMatchers("api/v1/QRdocs/upload", "api/v1/QRdocs/new-user", "api/v1/QRdocs/welcome").permitAll()
//                        .requestMatchers("api/v1/QRdocs/**").authenticated())
//                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
//                .build();
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/index.html", "/new-user").permitAll())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/**").permitAll())
//                        .requestMatchers("/**").authenticated())
//                .formLogin(formLogin -> formLogin
//                        .loginProcessingUrl("/login")
//                        .defaultSuccessUrl("/index.html", true))
                .build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}

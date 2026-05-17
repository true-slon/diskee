// package com.diskee.diskee_project.app.security;


// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.security.web.SecurityFilterChain;

// import jakarta.servlet.DispatcherType;
// import jakarta.servlet.http.HttpServletResponse;

// @Configuration
// @EnableWebSecurity
// public class WebSecurityConfig {

//     private static final String[] PUBLIC_URLS = {
//             "/",
//             "/app/v4",
//             "/app/v4/auth/register",
//             "/app/v4/auth/login",
//             "/app/v4/auth/remember-me",
//             "/app/v4/auth/email-confirm/*",
//             "/app/v4/auth/reset-password",
//             "/app/v4/auth/reset-password/*",
//             "/app/v4/auth/accept-invite/*",
//             "/swagger-ui.html",
//             "/swagger-ui/**",
//             "/v3/api-docs/**",
//             "/app/v4/file/{fileId}"
//     };
  
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

//                 .authorizeHttpRequests(auth -> auth
//                         .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
//                         .requestMatchers(PUBLIC_URLS).permitAll()
//                         .anyRequest().authenticated()
//                 )

//                 .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))

//                 .csrf(AbstractHttpConfigurer::disable)
//                 .formLogin(AbstractHttpConfigurer::disable)
//                 .rememberMe(AbstractHttpConfigurer::disable)
//                 .httpBasic(AbstractHttpConfigurer::disable)
//                 .logout(AbstractHttpConfigurer::disable)

//                 .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

//         return http.build();
//     }

//     private AuthenticationEntryPoint unauthorizedEntryPoint() {
//         return (request, response, authException) -> {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.setContentType("application/json");
//             response.setCharacterEncoding("UTF-8");
//             response.getWriter().write("{\"detail\":\"Unauthorized\"}");
//         };
//     }
// }
package com.diskee.diskee_project.app.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000"));
                config.setAllowedMethods(List.of("*"));
                config.setAllowedHeaders(List.of("*"));
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
        
        return http.build();
    }
}
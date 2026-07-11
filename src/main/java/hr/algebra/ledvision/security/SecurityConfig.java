package hr.algebra.ledvision.security;

import hr.algebra.ledvision.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthSuccessHandler successHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/packages/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/locations/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/tiers/**").permitAll()
                        .anyRequest().authenticated()


                )
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/packages",
                                "/packages/**",
                                "/locations",
                                "/locations/**",
                                "/cart/**",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .successHandler(successHandler)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID", "refreshToken")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
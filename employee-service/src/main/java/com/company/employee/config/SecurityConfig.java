package com.company.employee.config;

import com.company.employee.service.CustomUserDetailsService;
import com.company.employee.security.JwtAuthenticationSuccessHandler;
import com.company.employee.security.JwtAuthenticationFilter;
import com.company.employee.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
    var successHandler = new JwtAuthenticationSuccessHandler(jwtUtil, "ACCESS-TOKEN");
    var jwtFilter = new JwtAuthenticationFilter(jwtUtil);

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
            .requestMatchers("/login").permitAll()
            .requestMatchers("/api/employees/**").hasRole("ADMIN")
            .requestMatchers("/employees/**", "/").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .httpBasic(basic -> {})
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(successHandler)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .deleteCookies("ACCESS-TOKEN")
            .permitAll()
        )
        // remove remember-me and session usage for stateless JWT approach
        .sessionManagement(management -> management
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

    // Register JWT filter before username/password filter
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
    }

    // UserDetailsService is provided by CustomUserDetailsService component
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Removed custom authentication entry point to allow default browser login prompt
}

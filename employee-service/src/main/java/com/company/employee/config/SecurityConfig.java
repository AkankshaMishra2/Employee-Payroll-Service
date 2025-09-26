package com.company.employee.config;

import com.company.employee.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll() // Allow static resources
                .requestMatchers("/login").permitAll() // Allow access to login page
                .requestMatchers("/api/employees/**").hasRole("ADMIN") // Only ADMIN can access employee APIs
                .requestMatchers("/employees/**", "/").hasRole("ADMIN") // Only ADMIN can access employee web pages
                .anyRequest().authenticated() // All other endpoints require authentication
                )
                .httpBasic(basic -> {
                }) // Enable HTTP Basic Auth for API calls
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
                )
                .rememberMe(remember -> remember
                .key("employee-service-remember-me-key")
                .tokenValiditySeconds(2 * 60 * 60) // Remember for 2 hours
                .userDetailsService(customUserDetailsService)
                )
                .sessionManagement(management -> management
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .maximumSessions(10) // Increased concurrent sessions
                .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    // UserDetailsService is provided by CustomUserDetailsService component
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Removed custom authentication entry point to allow default browser login prompt
}

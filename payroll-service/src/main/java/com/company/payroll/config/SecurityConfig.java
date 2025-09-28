package com.company.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.company.payroll.security.JwtAuthenticationFilter;
import com.company.payroll.security.JwtAuthenticationSuccessHandler;
import com.company.payroll.security.JwtUtil;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        // create JWT filter using injected JwtUtil bean
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);
        var successHandler = new JwtAuthenticationSuccessHandler(jwtUtil, "ACCESS-TOKEN");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login").permitAll() // Allow access to login page
                .requestMatchers("/api/payroll/**").hasRole("HR") // Only HR can access payroll APIs
                .requestMatchers("/payrolls/**", "/").hasRole("HR") // Only HR can access payroll web pages
                .requestMatchers("/admin/**").hasRole("HR") // Only HR can access admin/cron job features
                .requestMatchers("/dashboard").hasRole("HR") // HR dashboard access
                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // Enable HTTP Basic Auth for API calls
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

        // Register JWT filter before username/password filter so API requests with Bearer token are authenticated
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails hr = User.builder()
                .username("hr")
                .password(passwordEncoder().encode("hr123"))
                .roles("HR")
                .build();

        return new InMemoryUserDetailsManager(admin, hr);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

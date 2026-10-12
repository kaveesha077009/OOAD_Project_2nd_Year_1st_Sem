package com.agroshop.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                // pages, styles, scripts and login are public. Data is protected below.
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/api/auth/**").permitAll()
                // the catalog can be browsed without an account
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/categories").permitAll()
                // stock work: admin and employee
                .requestMatchers("/api/products/low-stock", "/api/products/*/stock").hasAnyRole("ADMIN", "EMPLOYEE")
                // changing products and categories: admin only
                .requestMatchers("/api/products/**", "/api/categories/**").hasRole("ADMIN")
                // customers order for themselves
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("CUSTOMER")
                .requestMatchers("/api/orders/my").hasRole("CUSTOMER")
                // everything else about orders: staff
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

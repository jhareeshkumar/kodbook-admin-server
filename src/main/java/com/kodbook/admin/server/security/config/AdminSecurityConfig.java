package com.kodbook.admin.server.security.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.Duration;

@Configuration
public class AdminSecurityConfig {
    
    private final AdminServerProperties adminServerProperties;

    @Value("${remember.me.key}")
    private String rememberMeKey;

    public AdminSecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    @Bean
    public SecurityFilterChain springBootAdminServerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(adminServerProperties.getContextPath() + "/**")
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(adminServerProperties.path("/assets/**")).permitAll()
                        .requestMatchers(adminServerProperties.path("/**")).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage(adminServerProperties.path("/login")).permitAll()
                        .defaultSuccessUrl(adminServerProperties.path("/"), true)
                )
                .logout(logout -> logout
                        .logoutUrl(adminServerProperties.path("/logout")).permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/instances"), "POST"))
                        .ignoringRequestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/instances/*"), "DELETE"))
                        .ignoringRequestMatchers(new AntPathRequestMatcher(adminServerProperties.path("/logout"), "POST"))
                )
                .rememberMe(rememberMe -> rememberMe
                        .key(rememberMeKey)
                        .tokenValiditySeconds((int) Duration.ofDays(1).getSeconds())
                )
                .build();
    }
}

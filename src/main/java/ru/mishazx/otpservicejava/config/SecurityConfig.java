package ru.mishazx.otpservicejava.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import ru.mishazx.otpservicejava.exception.CustomAuthenticationFailureHandler;
import ru.mishazx.otpservicejava.exception.CustomAuthenticationSuccessHandler;
import ru.mishazx.otpservicejava.security.OtpAuthenticationFilter;
import ru.mishazx.otpservicejava.security.OtpAuthenticationProvider;

import java.util.Arrays;

/**
 * Этот класс настраивает, как работает безопасность и авторизация в приложении.
 * Здесь описано, кто и как может заходить на разные страницы, как происходит вход/выход и как шифруются пароли.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final OtpAuthenticationProvider otpProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.userDetailsService(userDetailsService);

        // Настраиваем CSRF защиту через куки и отключаем её для API запросов
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
        );

        http.authorizeHttpRequests(auth -> auth
                // Статические ресурсы доступны всем
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                // Страницы аутентификации доступны всем
                .requestMatchers("/login", "/register", "/forgot-password", "/error").permitAll()
                // Корневой путь доступен всем
                .requestMatchers("/").permitAll()
                // Пути OTP аутентификации доступны аутентифицированным пользователям
                .requestMatchers("/auth/otp/**").authenticated()
                // Все остальные страницы - только для аутентифицированных пользователей
                .anyRequest().authenticated()
        )
        .formLogin(form -> form
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(new CustomAuthenticationFailureHandler())
                .permitAll()
        )
        .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
        )
        .addFilterAfter(new OtpAuthenticationFilter(authenticationManager), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder());
        
        return new ProviderManager(Arrays.asList(daoProvider, otpProvider));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 
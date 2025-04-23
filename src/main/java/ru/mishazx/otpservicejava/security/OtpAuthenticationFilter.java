package ru.mishazx.otpservicejava.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OtpAuthenticationFilter extends BasicAuthenticationFilter {

    public OtpAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OtpAuthenticationToken) {
            OtpAuthenticationToken otpAuth = (OtpAuthenticationToken) authentication;
            if (!otpAuth.isOtpVerified() && !request.getRequestURI().startsWith("/auth/otp")) {
                response.sendRedirect("/auth/otp/method");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
} 
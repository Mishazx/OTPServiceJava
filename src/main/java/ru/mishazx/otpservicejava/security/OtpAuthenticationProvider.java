package ru.mishazx.otpservicejava.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class OtpAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof OtpAuthenticationToken) {
            OtpAuthenticationToken otpAuth = (OtpAuthenticationToken) authentication;
            if (otpAuth.isOtpVerified()) {
                return new OtpAuthenticationToken(
                    otpAuth.getPrincipal(),
                    otpAuth.getCredentials(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                    true
                );
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 
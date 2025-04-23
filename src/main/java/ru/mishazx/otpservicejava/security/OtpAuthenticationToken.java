package ru.mishazx.otpservicejava.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OtpAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final boolean otpVerified;

    public OtpAuthenticationToken(Object principal, Object credentials, boolean otpVerified) {
        super(principal, credentials);
        this.otpVerified = otpVerified;
    }

    public OtpAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, boolean otpVerified) {
        super(principal, credentials, authorities);
        this.otpVerified = otpVerified;
    }

    public boolean isOtpVerified() {
        return otpVerified;
    }
} 
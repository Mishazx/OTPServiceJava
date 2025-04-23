package ru.mishazx.otpservicejava.model.otp;

public enum OTPStatus {
    ACTIVE,   // Код активен и может быть использован
    EXPIRED,  // Срок действия кода истек
    USED      // Код был успешно использован
}

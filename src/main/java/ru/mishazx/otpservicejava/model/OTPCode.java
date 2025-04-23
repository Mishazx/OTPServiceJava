package ru.mishazx.otpservicejava.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mishazx.otpservicejava.model.otp.DeliveryMethod;
import ru.mishazx.otpservicejava.model.otp.OTPStatus;

import java.time.LocalDateTime;

/**
 * Модель представляет OTP-код, который используется для подтверждения операций.
 * Код может быть в одном из трех состояний: активный, истекший или использованный.
 */
@Entity
@Table(name = "otp_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Сам OTP-код
    @Column(nullable = false)
    private String code;

    // ID пользователя, которому принадлежит код
    @Column(nullable = false)
    private Long userId;

    // Идентификатор операции, к которой привязан код (опционально)
    private String operationId;

    // Статус кода: ACTIVE, EXPIRED, USED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OTPStatus status;

    // Время создания кода
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Время истечения срока действия кода
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Время использования кода (если был использован)
    private LocalDateTime usedAt;

    // Метод доставки кода: EMAIL, SMS, TELEGRAM, FILE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod deliveryMethod;

    // Адрес доставки (email, телефон, telegram ID и т.д.)
    @Column(nullable = false)
    private String deliveryAddress;

    /**
     * Проверяет, истек ли срок действия кода
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Отмечает код как использованный
     */
    public void markAsUsed() {
        this.status = OTPStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Отмечает код как истекший
     */
    public void markAsExpired() {
        this.status = OTPStatus.EXPIRED;
    }
}
package ru.mishazx.otpservicejava.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of Telegram bot functionality.
 * This allows the application to start even without Telegram bot credentials.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "false", matchIfMissing = false)
public class TelegramBotServiceStub {

    public TelegramBotServiceStub() {
        log.info("TelegramBotServiceStub initialized - Telegram functionality is disabled");
    }
    
    /**
     * Stub implementation of sendTelegramOtp that logs the OTP code but doesn't actually send it
     * 
     * @param contact Telegram chatId или username
     * @param otpCode OTP код для отправки
     * @return always returns false since this is a stub
     */
    public boolean sendTelegramOtp(String contact, String otpCode) {
        log.info("STUB: Would send OTP code {} to Telegram contact {}", otpCode, contact);
        return false;
    }
}

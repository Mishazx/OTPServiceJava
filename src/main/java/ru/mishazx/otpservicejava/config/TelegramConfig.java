package ru.mishazx.otpservicejava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import ru.mishazx.otpservicejava.service.TelegramBotService;
import ru.mishazx.otpservicejava.service.TelegramBotServiceStub;

/**
 * Configuration for Telegram bot functionality.
 * This allows the application to start even without Telegram bot credentials.
 */
@Configuration
@Slf4j
public class TelegramConfig {

    @Value("${telegram.enabled}")
    private boolean telegramEnabled;

    /**
     * Creates a stub implementation of TelegramBotService when Telegram is disabled
     */
    @Bean
    @ConditionalOnProperty(name = "telegram.enabled", havingValue = "false", matchIfMissing = false)
    public TelegramBotServiceStub telegramBotServiceStub() {
        log.info("Creating TelegramBotServiceStub bean because telegram.enabled = {}", telegramEnabled);
        return new TelegramBotServiceStub();
    }
}

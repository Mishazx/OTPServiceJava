package ru.mishazx.otpservicejava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import ru.mishazx.otpservicejava.service.TelegramBotService;
import ru.mishazx.otpservicejava.repository.UserRepository;

/**
 * Configuration for Telegram bot functionality.
 */
@Configuration
@Slf4j
public class TelegramConfig {

    @Value("${telegram.enabled}")
    private boolean telegramEnabled;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    @ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
    public TelegramBotService telegramBotService(UserRepository userRepository) {
        log.info("Telegram bot is enabled");
        return new TelegramBotService(userRepository, botToken);
    }
}

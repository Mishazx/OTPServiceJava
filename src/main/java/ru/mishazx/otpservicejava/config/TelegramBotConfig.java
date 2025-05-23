package ru.mishazx.otpservicejava.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.mishazx.otpservicejava.service.TelegramBotService;
import ru.mishazx.otpservicejava.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация для интеграции с Telegram ботом.
 * Создает и регистрирует TelegramBotService в Telegram API.
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramBotConfig {

    @Value("${telegram.enabled}")
    private boolean telegramEnabled;
    
    @Value("${telegram.bot.token:}")
    private String botToken;
    
    @Value("${telegram.bot.name:}")
    private String botName;
    
    private TelegramBotService telegramBotService;
    
    @PostConstruct
    public void init() {
        log.info("TelegramBotConfig initialized with telegram.enabled={}", telegramEnabled);
        log.info("Bot token is {}set, bot name is {}set", 
                botToken != null && !botToken.isEmpty() ? "" : "NOT ",
                botName != null && !botName.isEmpty() ? "" : "NOT ");
    }

    @Bean
    public TelegramBotService telegramBotService(UserRepository userRepository) {
        log.info("Creating TelegramBotService bean");
        if (botToken != null && !botToken.isEmpty()) {
            this.telegramBotService = new TelegramBotService(userRepository, botToken);
        } else {
            log.warn("Bot token is not set, creating TelegramBotService without token");
            this.telegramBotService = new TelegramBotService(userRepository);
        }
        return this.telegramBotService;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        try {
            if (telegramBotService != null && botToken != null && !botToken.isEmpty()) {
                log.info("Registering Telegram bot with Telegram API");
                TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(telegramBotService);
                log.info("Telegram bot successfully registered with name: {}", telegramBotService.getBotUsername());
                return api;
            } else {
                log.warn("TelegramBotService is null or bot token is not provided, skipping registration with Telegram API.");
            }
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
        }
        return null;
    }
}
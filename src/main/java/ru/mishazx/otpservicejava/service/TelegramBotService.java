package ru.mishazx.otpservicejava.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mishazx.otpservicejava.model.User;
import ru.mishazx.otpservicejava.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramBotService extends TelegramLongPollingBot {
    private final UserRepository userRepository;

    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    // Хранилище для связи Telegram ID и пользователей
    private final Map<Long, String> registeredUsers = new HashMap<>();

    public TelegramBotService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("TelegramBotService создан username ={}.", botUsername);
    }

    public TelegramBotService(UserRepository userRepository, String botToken) {
        super(botToken);
        this.userRepository = userRepository;
        this.botToken = botToken;
        log.info("TelegramBotService создан с token и username ={}.", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Bot token is not set. Using a dummy token for development.");
            return "dummy_token";
        }
        return botToken;
    }

    // Инициализация меню команд бота
    @PostConstruct
    public void initCommands() {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Bot token is not set. Skipping command initialization.");
            return;
        }
        
        List<BotCommand> commandList = new ArrayList<>();
        try {
            commandList.add(new BotCommand("/start", "Запустить бота"));
            commandList.add(new BotCommand("/code", "Получить OTP-код"));
            commandList.add(new BotCommand("/link", "Привязать аккаунт"));
            commandList.add(new BotCommand("/help", "Получить помощь"));

            execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации команд бота", e);
            log.error("Ошибка при инициализации команд бота. Команды: {}. Сообщение об ошибке: {}. Стек вызовов: {}",
                    commandList, e.getMessage(), e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.info("Получено сообщение: {} от chatId: {}", messageText, chatId);

            // Обработка команды запуска с параметром
            if (messageText.startsWith("/start")) {
                String[] parts = messageText.split(" ", 2);

                // Если есть параметр после /start (для deep linking)
                if (parts.length > 1 && parts[1].startsWith("link_")) {
                    String token = parts[1].substring(5); // Извлекаем токен после "link_"
                    linkAccount(chatId, token);
                } else {
                    // Получаем username, если он есть
                    String username = update.getMessage().getFrom().getUserName();
                    sendWelcomeMessage(chatId, username);
                }
            } else if (messageText.equals("/code") || messageText.equals("Получить код")) {
                sendOtpCode(chatId);
            } else if (messageText.equals("/help") || messageText.equals("Помощь")) {
                sendHelpMessage(chatId);
            } else if (messageText.startsWith("/link_")) {
                String token = messageText.substring(6); // Получаем токен после "/link_"
                linkAccount(chatId, token);
            } else if (messageText.equals("/link") || messageText.equals("Привязать аккаунт")) {
                // Генерируем новый токен и отправляем пользователю код для привязки
                String token = UUID.randomUUID().toString();
                
                // Сохраняем информацию о сгенерированном токене
                log.info("Генерация нового токена привязки для chatId: {}: {}", chatId, token);
            }
        }
    }

    private void sendOtpCode(long chatId) {
    }

    private void linkAccount(long chatId, String token) {
    }

    private void sendWelcomeMessage(long chatId, String username) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Добро пожаловать, " + (username != null ? "@" + username : "пользователь") + 
                            "!\n\nЭтот бот используется для двухфакторной аутентификации в системе OTP Service.\n\n" +
                            "Для привязки аккаунта используйте команду /register <ваш_логин>");
            execute(message);
            
            // Сохраняем связь Telegram username и chatId
            if (username != null) {
                registeredUsers.put(chatId, username.toLowerCase());
            }
        } catch (TelegramApiException e) {
            log.error("Error sending welcome message", e);
        }
    }

    private void handleRegistration(long chatId, String telegramUsername, String messageText) {
        try {
            String[] parts = messageText.split("\\s+", 2);
            if (parts.length < 2) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Пожалуйста, укажите ваш логин: /register <ваш_логин>");
                execute(message);
                return;
            }
            
            String login = parts[1].trim();
            User user = userRepository.findByUsername(login).orElse(null);
            
            if (user == null) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Пользователь с логином '" + login + "' не найден.");
                execute(message);
                return;
            }
            
            // Сохраняем связь Telegram username и chatId
            if (telegramUsername != null) {
                registeredUsers.put(chatId, telegramUsername.toLowerCase());
                log.info("User {} registered Telegram account @{}", login, telegramUsername);
            }
            
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Аккаунт успешно привязан! Теперь вы будете получать коды подтверждения через этот чат.");
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error handling registration", e);
        }
    }

    private void sendHelpMessage(long chatId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Доступные команды:\n" +
                            "/start - Начать работу с ботом\n" +
                            "/register <ваш_логин> - Привязать ваш аккаунт к Telegram");
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending help message", e);
        }
    }

    /**
     * Отправляет OTP код пользователю через Telegram
     * 
     * @param contact Telegram chatId или username
     * @param otpCode OTP код для отправки
     * @return true если отправка успешна
     */
    public boolean sendTelegramOtp(String contact, String otpCode) {
        try {
            // Пытаемся преобразовать контакт в chatId
            long chatId;
            try {
                chatId = Long.parseLong(contact);
            } catch (NumberFormatException e) {
                log.error("Невозможно преобразовать контакт в chatId: {}", contact);
                return false;
            }
            
            SendMessage message = createMessageWithKeyboard(chatId,
                    "🔐 Ваш код подтверждения: *" + otpCode + "*\n\n" +
                            "Код действителен в течение 5 минут. " +
                            "Введите его на странице авторизации.");

            message.enableMarkdown(true); // Для выделения кода жирным

            execute(message);
            log.info("OTP код успешно отправлен пользователю через Telegram");
            return true;
        } catch (Exception e) {
            log.error("Ошибка при отправке OTP-кода через Telegram: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // Создание сообщения с клавиатурой
    private SendMessage createMessageWithKeyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true); // Компактный размер
        keyboardMarkup.setOneTimeKeyboard(false); // Клавиатура не исчезает после использования

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Получить код"));
        row1.add(new KeyboardButton("Привязать аккаунт"));

        // Второй ряд кнопок
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Помощь"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    private Long getChatIdByUsername(String telegramUsername) {
        // Убираем @ в начале имени пользователя, если он есть
        if (telegramUsername.startsWith("@")) {
            telegramUsername = telegramUsername.substring(1);
        }
        
        // Находим chatId по имени пользователя
        for (Map.Entry<Long, String> entry : registeredUsers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(telegramUsername)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

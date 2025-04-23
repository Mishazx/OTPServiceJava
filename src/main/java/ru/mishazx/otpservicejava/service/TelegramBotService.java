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
import ru.mishazx.otpservicejava.model.OTPCode;
import ru.mishazx.otpservicejava.model.User;
import ru.mishazx.otpservicejava.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramBotService extends TelegramLongPollingBot {
//    private final UserRepository userRepository;

    @Value("${telegram.bot.name}")
//    ${TG_TOKEN:default_value}"
    private String botUsername;



    public TelegramBotService(
            UserRepository userRepository,
            @Value("${telegram.bot.token}") String botToken
    ) {
        super(botToken);
        log.info("TelegramBotService создан token = {}, username ={}.", botUsername, botToken);
        log.info("TelegramBotService инициализирован с токеном: {}", botToken);
//        this.userRepository = userRepository;

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    // Инициализация меню команд бота
    @PostConstruct
    public void initCommands() {
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
                    sendWelcomeMessage(chatId);
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
                
                // Очищаем предыдущие токены для этого чата (если есть)
//                for (Map.Entry<String, Long> entry : linkTokens.entrySet()) {
//                    if (entry.getValue().equals(chatId)) {
//                        log.info("Удаляем устаревший токен для chatId {}: {}", chatId, entry.getKey());
//                        linkTokens.remove(entry.getKey());
//                    }
//                }
                
//                sendLinkCode(chatId, token);
            }
        }
    }

    private void sendOtpCode(long chatId) {
    }

    private void linkAccount(long chatId, String token) {
    }

    private void sendWelcomeMessage(long chatId) {
        SendMessage message = createMessageWithKeyboard(chatId,
                "👋 Добро пожаловать в сервис OTP-авторизации!\n\n" +
                        "Этот бот поможет вам получать коды подтверждения для авторизации в системе.\n\n" +
                        "Что вы можете сделать:\n" +
                        "🔹 Нажмите кнопку «Получить код» для генерации нового OTP-кода\n" +
                        "🔹 Если вы уже привязали свой аккаунт, коды будут автоматически приходить при запросе с сайта\n" +
                        "🔹 Для получения дополнительной информации нажмите «Помощь»");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке приветственного сообщения", e);
        }
    }

    private void sendHelpMessage(long chatId) {
        SendMessage message = createMessageWithKeyboard(chatId,
                "ℹ️ Справка по использованию бота:\n\n" +
                        "1️⃣ Для получения кода нажмите кнопку «Получить код» или используйте команду /code\n\n" +
                        "2️⃣ Если ваш аккаунт уже привязан к Telegram, система сможет автоматически отправлять вам коды\n\n" +
                        "3️⃣ Коды действительны в течение 5 минут после генерации\n\n" +
                        "4️⃣ При возникновении проблем обратитесь в техническую поддержку");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке справочного сообщения", e);
        }
    }

//    private void linkAccount(long chatId, String token) {
//        boolean success = telegramLinkService.linkTelegramAccount(token, chatId);
//
//        if (success) {
//            SendMessage message = createMessageWithKeyboard(chatId,
//                    "✅ Ваш Telegram успешно привязан к аккаунту!\n\n" +
//                            "Теперь вы можете получать коды подтверждения через этот чат. " +
//                            "Система будет автоматически отправлять вам коды при авторизации.");
//
//            try {
//                execute(message);
//            } catch (TelegramApiException e) {
//                log.error("Ошибка при отправке сообщения об успешной привязке", e);
//            }
//        } else {
//            // Если привязка не удалась, генерируем код для привязки вручную
//            String newToken = UUID.randomUUID().toString();
//            sendLinkCode(chatId, newToken);
//
//            SendMessage message = createMessageWithKeyboard(chatId,
//                    "ℹ️ Чтобы привязать Telegram к вашему аккаунту, скопируйте код выше и введите его на сайте.");
//
//            try {
//                execute(message);
//            } catch (TelegramApiException e) {
//                log.error("Ошибка при отправке сообщения о неудачной привязке", e);
//            }
//        }
//    }

//    public void sendOtpCode(long chatId) {
//        String otpCode = otpGenerator.generateOTP();
//        activeOtpCodes.put(chatId, otpCode);
//
//        SendMessage message = createMessageWithKeyboard(chatId,
//                "🔐 Ваш код подтверждения: *" + otpCode + "*\n\n" +
//                        "Код действителен в течение 5 минут. " +
//                        "Введите его на странице авторизации.");
//
//        message.enableMarkdown(true); // Для выделения кода жирным
//
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            log.error("Ошибка при отправке OTP-кода", e);
//        }
//    }

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

    // Метод для внешнего вызова, чтобы отправить код по chatId
//    public boolean sendOtpForUser(long chatId) {
//        try {
//            // Найти пользователя по chatId
//            User user = userRepository.findByTelegramChatId(chatId).orElse(null);
//            if (user == null) {
//                log.error("Пользователь с chatId {} не найден", chatId);
//                return false;
//            }
//
//            // Сохраняем в базе данных через OtpService и получаем созданный код без отправки через Telegram
//            OTPCode otpCode = OTPService.generateTelegramOtpWithoutSending(user, String.valueOf(chatId));
//
//            // Получаем код из сгенерированного OtpCode
//            String code = otpCode.getCode();
//
//            // Сохраняем в памяти для быстрого доступа
//            activeOtpCodes.put(chatId, code);
//
//            // Отправляем код пользователю напрямую через Telegram
//            SendMessage message = createMessageWithKeyboard(chatId,
//                    "🔐 Ваш код подтверждения: *" + code + "*\n\n" +
//                            "Код действителен в течение 5 минут. " +
//                            "Введите его на странице авторизации.");
//
//            message.enableMarkdown(true); // Для выделения кода жирным
//
//            try {
//                execute(message);
//                log.info("OTP код {} успешно отправлен пользователю через Telegram", code);
//                return true;
//            } catch (TelegramApiException e) {
//                log.error("Ошибка при отправке сообщения с кодом: {}", e.getMessage(), e);
//                return false;
//            }
//        } catch (Exception e) {
//            log.error("Ошибка при отправке OTP-кода пользователю: {}", e.getMessage(), e);
//            return false;
//        }
//    }

    // Перегруженный метод для приема chatId как String и кода
//    public void sendOtpCode(String chatIdStr, String code) {
//        try {
//            long chatId = Long.parseLong(chatIdStr);
//            SendMessage message = createMessageWithKeyboard(chatId,
//                    "🔐 Ваш код подтверждения: *" + code + "*\n\n" +
//                            "Код действителен в течение 5 минут. " +
//                            "Введите его на странице авторизации.");
//
//            message.enableMarkdown(true); // Для выделения кода жирным
//
//            try {
//                execute(message);
//            } catch (TelegramApiException e) {
//                log.error("Ошибка при отправке сообщения", e);
//            }
//        } catch (NumberFormatException e) {
//            log.error("Невозможно преобразовать chatId в числовой формат: {}", chatIdStr, e);
//        }
//    }

    // Метод для проверки кода
//    public boolean verifyOtp(long chatId, String code) {
//        String storedCode = activeOtpCodes.get(chatId);
//        if (storedCode != null && storedCode.equals(code)) {
//            activeOtpCodes.remove(chatId); // Удаляем использованный код
//            return true;
//        }
//        return false;
//    }

    // Метод для отправки специального кода привязки с chatId и токеном
//    public void sendLinkCode(long chatId, String token) {
//        // Сохраняем токен в хранилище с привязкой к chatId
//        linkTokens.put(token, chatId);
//
//        // Создаем короткий код для удобства (используем просто токен)
//        String linkCode = token;
//
//        SendMessage message = createMessageWithKeyboard(chatId,
//                "🔑 Ваш код для привязки аккаунта: *" + linkCode + "*\n\n" +
//                        "Скопируйте этот код и вставьте его на сайте в поле 'Код из Telegram'.\n" +
//                        "⚠️ Код действителен только для одной привязки.");
//
//        message.enableMarkdown(true); // Для выделения кода жирным
//
//        try {
//            execute(message);
//            log.info("Отправлен код привязки для chatId: {} с токеном: {}", chatId, token);
//        } catch (TelegramApiException e) {
//            log.error("Ошибка при отправке кода привязки", e);
//        }
//    }
    
    // Метод для получения chatId по токену
//    public Long getChatIdByToken(String token) {
//        log.info("Поиск chatId по токену: {}", token);
//        Long chatId = linkTokens.get(token);
//
//        if (chatId != null) {
//            log.info("Найден chatId: {} для токена: {}", chatId, token);
//        } else {
//            log.warn("ChatId не найден для токена: {}. Текущие токены: {}", token, linkTokens.keySet());
//        }
//
//        return chatId;
//    }
    
    // Временный метод для отладки - вывод всех активных токенов
//    public Map<String, Long> getActiveLinkTokens() {
//        log.info("Активные токены: {}", linkTokens);
//        return linkTokens;
//    }

    // Метод для удаления токена из хранилища
//    public void removeToken(String token) {
//        if (linkTokens.containsKey(token)) {
//            Long chatId = linkTokens.remove(token);
//            log.info("Токен {} удален из хранилища после успешной привязки для chatId {}", token, chatId);
//        } else {
//            log.warn("Попытка удалить несуществующий токен: {}", token);
//        }
//    }

//    public void sendOtpForUser(String username) {
//        User user = userRepository.findByUsername(username).orElse(null);
//        if (user == null || user.getTelegramChatId() == null) {
//            log.warn("Невозможно отправить OTP код: пользователь {} не существует или не привязан к Telegram", username);
//            return;
//        }
//
//        long chatId = user.getTelegramChatId();
//        String otpCode = otpGenerator.generateOTP();
//
//        // Сохраняем в памяти для быстрого доступа
//        activeOtpCodes.put(chatId, otpCode);
//
//        String text = String.format("Ваш код подтверждения: *%s*\n\nКод действителен в течение %d минут.",
//                                   otpCode, OTP_EXPIRATION_MINUTES);
//
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText(text);
//        message.enableMarkdown(true);
//
//        try {
//            execute(message);
//            log.info("OTP код успешно отправлен пользователю {} через Telegram", username);
//        } catch (TelegramApiException e) {
//            log.error("Ошибка при отправке OTP кода в Telegram: {}", e.getMessage());
//        }
//    }
}

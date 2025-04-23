# OTP Service Java

Сервис для генерации и проверки одноразовых паролей (OTP) с поддержкой различных каналов доставки: Email, Telegram и SMS.

## Настройка окружения разработки

### Профиль разработки (dev)

Для локальной разработки используется профиль `dev`. Чтобы настроить его:

1. Создайте файл `src/main/resources/application-dev.yml` на основе шаблона `application-dev.yml.example`:

```bash
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

2. Отредактируйте файл `application-dev.yml` и заполните следующие параметры:

```yaml
# Настройки Telegram бота
telegram:
  enabled: true  # Установите false, если не хотите использовать Telegram
  bot:
    token: your_telegram_bot_token_here  # Получите у @BotFather
    name: your_telegram_bot_name_here

# Настройки почтового сервера
spring:
  mail:
    username: your_email_username_here
    password: your_email_password_here
```

### Альтернативный способ: переменные окружения

Вместо файла `application-dev.yml` можно использовать переменные окружения:

```bash
export TG_TOKEN=your_telegram_bot_token_here
export TG_BOT_NAME=your_telegram_bot_name_here
export MAIL_USERNAME=your_email_username_here
export MAIL_PASSWORD=your_email_password_here
export TELEGRAM_ENABLED=true
```

## Запуск приложения

```bash
./mvnw spring-boot:run
```

## Функциональность

- Генерация OTP кодов
- Проверка OTP кодов
- Отправка кодов через Email
- Отправка кодов через Telegram бота
- Поддержка SMS (в разработке)

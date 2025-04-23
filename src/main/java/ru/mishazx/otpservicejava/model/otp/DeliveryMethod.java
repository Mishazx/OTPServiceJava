package ru.mishazx.otpservicejava.model.otp;

public enum DeliveryMethod {
    EMAIL,    // Отправка по электронной почте
    SMS,      // Отправка через SMS (SMPP)
    TELEGRAM, // Отправка через Telegram
    FILE      // Сохранение в файл
}

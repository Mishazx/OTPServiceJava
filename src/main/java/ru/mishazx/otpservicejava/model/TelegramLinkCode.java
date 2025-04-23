package ru.mishazx.otpservicejava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telegram_link_code")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkCode {
    @Id
    private Long id;

    @Column(unique = true)
    private Long telegramUserId;

    @Column(unique = true)
    private String telegramUserName;





}

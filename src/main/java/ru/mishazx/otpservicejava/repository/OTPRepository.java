package ru.mishazx.otpservicejava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mishazx.otpservicejava.model.OTPCode;
import ru.mishazx.otpservicejava.model.otp.OTPStatus;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPCode, Long> {
    // Найти активный код по ID пользователя и коду
    Optional<OTPCode> findByUserIdAndCodeAndStatus(Long userId, String code, OTPStatus status);

    // Найти активный код по ID пользователя, коду, статусу и ID операции
    Optional<OTPCode> findByUserIdAndCodeAndStatusAndOperationId(Long userId, String code, OTPStatus status, String operationId);

    // Найти последний активный код пользователя
    Optional<OTPCode> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OTPStatus status);

    // Удалить все коды пользователя
    void deleteByUserId(Long userId);

    // Найти все истекшие, но не отмеченные как таковые коды
    //    List<OTPCode> findByStatusAndExpiresAtLessThan(OTPStatus status, LocalDateTime now);

    // Обновить статус для истекших кодов
    //    @Modifying
    //    @Transactional
    //    @Query("UPDATE OTPCode o SET o.status = OTPStatus.EXPIRED WHERE o.status = OTPStatus.ACTIVE AND o.expiresAt < :now")
    //    int markExpiredCodes(LocalDateTime now);
}
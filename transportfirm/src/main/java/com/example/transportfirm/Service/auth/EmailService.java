package com.example.transportfirm.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Изпраща имейл при създаване на служител от администратор
     */
    public void sendAdminCreatedUserEmail(
            String toEmail,
            String username,
            String tempPassword,
            String otp
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TransTrack – Създаден достъп до системата");

        message.setText(
                "Здравей,\n\n" +
                        "За теб беше създаден служебен акаунт в системата TransTrack от администратор.\n\n" +
                        "Данни за вход:\n" +
                        "-----------------------------------\n" +
                        "Потребителско име: " + username + "\n" +
                        "Временна парола: " + tempPassword + "\n" +
                        "-----------------------------------\n\n" +
                        "Код за верификация (OTP): " + otp + "\n\n" +
                        "⚠️ Моля, при първото си влизане:\n" +
                        "- въведи OTP кода за потвърждение на акаунта\n" +
                        "- смени временната си парола с нова\n\n" +
                        "Ако не разпознаваш този имейл, моля свържи се с администратор.\n\n" +
                        "Поздрави,\n" +
                        "Екипът на TransTrack"
        );

        mailSender.send(message);
    }

    /**
     * Имейл за възстановяване на парола
     */
    public void sendResetOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TransTrack – Възстановяване на парола");

        message.setText(
                "Здравей,\n\n" +
                        "Получихме заявка за възстановяване на паролата за твоя акаунт в TransTrack.\n\n" +
                        "Код за потвърждение (OTP): " + otp + "\n\n" +
                        "Кодът е валиден за ограничено време.\n" +
                        "Ако не си заявявал смяна на парола, можеш спокойно да игнорираш този имейл.\n\n" +
                        "Поздрави,\n" +
                        "Екипът на TransTrack"
        );

        mailSender.send(message);
    }

    /**
     * Имейл за първоначална верификация на акаунт
     */
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TransTrack – Потвърждение на акаунт");

        message.setText(
                "Здравей,\n\n" +
                        "За да активираш своя акаунт в TransTrack, използвай следния код за потвърждение:\n\n" +
                        "OTP код: " + otp + "\n\n" +
                        "Ако не си правил регистрация или не очакваш този имейл, моля игнорирай съобщението.\n\n" +
                        "Поздрави,\n" +
                        "Екипът на TransTrack"
        );

        mailSender.send(message);
    }
}

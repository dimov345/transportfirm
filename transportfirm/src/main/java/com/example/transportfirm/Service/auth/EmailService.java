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
     * Reminder за изтекли/изтичащи документи на шофьор — изпраща се на самия шофьор
     */
    public void sendDriverDocumentReminderEmail(String toEmail, String driverName, java.util.List<String[]> items) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TransTrack – Напомняне за документи на шофьор");

        StringBuilder sb = new StringBuilder();
        sb.append("Здравей, ").append(driverName).append(",\n\n");
        sb.append("Следните твои документи изтичат или са вече изтекли:\n\n");
        for (String[] item : items) {
            // item: {docType, status, days, expiryDate}
            String status = "expired".equals(item[1])
                    ? "ИЗТЕКЪЛ преди " + item[2] + " дни (изтекъл на " + item[3] + ")"
                    : (Long.parseLong(item[2]) == 0
                        ? "изтича ДНЕС (" + item[3] + ")"
                        : "изтича след " + item[2] + " дни (" + item[3] + ")");
            sb.append("  • ").append(item[0]).append(": ").append(status).append("\n");
        }
        sb.append("\nМоля, подновете документите навреме.\n\n");
        sb.append("Поздрави,\nЕкипът на TransTrack");

        message.setText(sb.toString());
        mailSender.send(message);
    }

    /**
     * Reminder за изтекли/изтичащи документи на ППС — изпраща се на спедитора (и/или admin)
     */
    public void sendVehicleDocumentReminderEmail(String toEmail, String vehiclePlate, java.util.List<String[]> items) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TransTrack – Напомняне за документи на ППС " + vehiclePlate);

        StringBuilder sb = new StringBuilder();
        sb.append("Здравейте,\n\n");
        sb.append("Следните документи за ППС ").append(vehiclePlate).append(" изтичат или са вече изтекли:\n\n");
        for (String[] item : items) {
            // item: {docType, status, days, expiryDate}
            String status = "expired".equals(item[1])
                    ? "ИЗТЕКЪЛ преди " + item[2] + " дни (изтекъл на " + item[3] + ")"
                    : (Long.parseLong(item[2]) == 0
                        ? "изтича ДНЕС (" + item[3] + ")"
                        : "изтича след " + item[2] + " дни (" + item[3] + ")");
            sb.append("  • ").append(item[0]).append(": ").append(status).append("\n");
        }
        sb.append("\nМоля, предприемете необходимите действия.\n\n");
        sb.append("Поздрави,\nЕкипът на TransTrack");

        message.setText(sb.toString());
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

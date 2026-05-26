package com.example.transportfirm.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends transactional emails via the Brevo HTTP API (POST /v3/smtp/email).
 *
 * Uses HTTPS on port 443 — works on Railway which blocks all outbound SMTP ports
 * (25, 465, 587, 2525).
 *
 * Required env var in Railway:
 *   BREVO_API_KEY  — from Brevo dashboard → Settings → SMTP & API → API Keys
 *
 * Optional env vars (have sensible defaults):
 *   BREVO_FROM_EMAIL  — verified sender address  (default: transtrackbg@gmail.com)
 *   BREVO_FROM_NAME   — display name             (default: TransTrack)
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient = RestClient.create();

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${BREVO_FROM_EMAIL:transtrackbg@gmail.com}")
    private String fromEmail;

    @Value("${BREVO_FROM_NAME:TransTrack}")
    private String fromName;

    // ───────────────────────────────────────────────
    //  Public email methods (all @Async — non-blocking)
    // ───────────────────────────────────────────────

    /** Имейл при създаване на служител от администратор */
    @Async
    public void sendAdminCreatedUserEmail(String toEmail, String username,
                                          String tempPassword, String otp) {
        String body =
                "Здравей,\n\n" +
                "За теб беше създаден служебен акаунт в системата TransTrack.\n\n" +
                "Данни за вход:\n" +
                "-----------------------------------\n" +
                "Потребителско име: " + username + "\n" +
                "Временна парола:   " + tempPassword + "\n" +
                "-----------------------------------\n\n" +
                "Код за верификация (OTP): " + otp + "\n\n" +
                "⚠️ При първото влизане:\n" +
                "- въведи OTP кода за потвърждение\n" +
                "- смени временната парола с нова\n\n" +
                "Ако не разпознаваш този имейл, свържи се с администратор.\n\n" +
                "Поздрави,\nЕкипът на TransTrack";

        send(toEmail, "TransTrack – Създаден достъп до системата", body);
    }

    /** Имейл за възстановяване на парола */
    @Async
    public void sendResetOtpEmail(String toEmail, String otp) {
        String body =
                "Здравей,\n\n" +
                "Получихме заявка за възстановяване на паролата ти в TransTrack.\n\n" +
                "Код за потвърждение (OTP): " + otp + "\n\n" +
                "Кодът е валиден за ограничено време.\n" +
                "Ако не си заявявал смяна на парола, игнорирай този имейл.\n\n" +
                "Поздрави,\nЕкипът на TransTrack";

        send(toEmail, "TransTrack – Възстановяване на парола", body);
    }

    /** Имейл за потвърждение на акаунт (OTP) */
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        String body =
                "Здравей,\n\n" +
                "За да активираш акаунта си в TransTrack, използвай следния код:\n\n" +
                "OTP код: " + otp + "\n\n" +
                "Ако не си правил регистрация, игнорирай това съобщение.\n\n" +
                "Поздрави,\nЕкипът на TransTrack";

        send(toEmail, "TransTrack – Потвърждение на акаунт", body);
    }

    /** Reminder за документи на шофьор */
    @Async
    public void sendDriverDocumentReminderEmail(String toEmail, String driverName,
                                                List<String[]> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("Здравей, ").append(driverName).append(",\n\n");
        sb.append("Следните твои документи изтичат или са вече изтекли:\n\n");
        appendItems(sb, items);
        sb.append("\nМоля, подновете документите навреме.\n\nПоздрави,\nЕкипът на TransTrack");

        send(toEmail, "TransTrack – Напомняне за документи на шофьор", sb.toString());
    }

    /** Reminder за документи на ППС */
    @Async
    public void sendVehicleDocumentReminderEmail(String toEmail, String vehiclePlate,
                                                 List<String[]> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("Здравейте,\n\n");
        sb.append("Следните документи за ППС ").append(vehiclePlate)
          .append(" изтичат или са вече изтекли:\n\n");
        appendItems(sb, items);
        sb.append("\nМоля, предприемете необходимите действия.\n\nПоздрави,\nЕкипът на TransTrack");

        send(toEmail, "TransTrack – Напомняне за документи на ППС " + vehiclePlate, sb.toString());
    }

    // ───────────────────────────────────────────────
    //  Private helpers
    // ───────────────────────────────────────────────

    /**
     * Sends a plain-text email via Brevo HTTP API.
     * Skips silently if BREVO_API_KEY is not configured (local dev without key).
     */
    private void send(String toEmail, String subject, String textContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("BREVO_API_KEY not set — skipping email to {}", toEmail);
            return;
        }

        Map<String, Object> payload = Map.of(
            "sender",      Map.of("name", fromName, "email", fromEmail),
            "to",          List.of(Map.of("email", toEmail)),
            "subject",     subject,
            "textContent", textContent
        );

        try {
            restClient.post()
                .uri(BREVO_URL)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

            log.info("Email sent via Brevo API to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email via Brevo API to {}: {}", toEmail, e.getMessage());
        }
    }

    private void appendItems(StringBuilder sb, List<String[]> items) {
        for (String[] item : items) {
            // item: {docType, status, days, expiryDate}
            String status = "expired".equals(item[1])
                    ? "ИЗТЕКЪЛ преди " + item[2] + " дни (" + item[3] + ")"
                    : (Long.parseLong(item[2]) == 0
                        ? "изтича ДНЕС (" + item[3] + ")"
                        : "изтича след " + item[2] + " дни (" + item[3] + ")");
            sb.append("  • ").append(item[0]).append(": ").append(status).append("\n");
        }
    }
}

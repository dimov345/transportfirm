package com.example.transportfirm.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    @Value("${twilio.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    public SmsService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /**
     * Изпраща SMS чрез Twilio REST API.
     * Не хвърля изключения — всички грешки се логват.
     */
    public void sendSms(String phone, String text) {
        if (!enabled) {
            log.debug("SMS изпращането е изключено (twilio.enabled=false). Пропускам SMS до: {}", phone);
            return;
        }
        if (accountSid == null || accountSid.isBlank() ||
            authToken  == null || authToken.isBlank()  ||
            fromNumber == null || fromNumber.isBlank()) {
            log.warn("Twilio не е конфигуриран (account-sid / auth-token / from-number). Пропускам SMS до: {}", phone);
            return;
        }

        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            log.warn("Невалиден телефонен номер: '{}' — пропускам SMS", phone);
            return;
        }

        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(accountSid, authToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To",   normalizedPhone);
            body.add("From", fromNumber);
            body.add("Body", text);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS изпратен успешно до: {}", normalizedPhone);
            } else {
                log.warn("Twilio върна статус {} за номер {}: {}",
                        response.getStatusCode(), normalizedPhone, response.getBody());
            }
        } catch (Exception e) {
            log.error("Грешка при изпращане на SMS до {}: {}", normalizedPhone, e.getMessage(), e);
        }
    }

    /**
     * Нормализира телефонни номера до E.164 формат.
     * Twilio изисква "+" префикс.
     *
     * <ul>
     *   <li>+359XXXXXXXXX  → as-is (already E.164, stored by FE country-code picker)</li>
     *   <li>00359XXXXXXX   → +359XXXXXXXXX</li>
     *   <li>0XXXXXXXXX     → +359XXXXXXXXX (Bulgarian local format)</li>
     *   <li>XXXXXXXXX (9d) → +359XXXXXXXXX (Bulgarian without leading zero)</li>
     *   <li>anything else  → "+" + digits</li>
     * </ul>
     */
    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;

        // Strip only spaces and dashes — keep + and digits intact
        String stripped = phone.replaceAll("[\\s\\-]", "");
        if (stripped.isEmpty()) return null;

        // Already E.164 (starts with +): trust it, just clean spaces
        if (stripped.startsWith("+")) {
            // Remove non-digit chars after the +
            String digits = stripped.substring(1).replaceAll("\\D", "");
            return digits.isEmpty() ? null : "+" + digits;
        }

        // Strip remaining non-digit chars
        String p = stripped.replaceAll("\\D", "");
        if (p.isEmpty()) return null;

        if (p.startsWith("00")) {
            p = p.substring(2);            // 00359... → 359...
        }
        if (p.startsWith("359") && p.length() >= 11) {
            return "+" + p;                // 359888123456 → +359888123456
        }
        if (p.startsWith("0") && p.length() == 10) {
            return "+359" + p.substring(1); // 0888123456 → +359888123456
        }
        if (p.length() == 9) {
            return "+359" + p;             // 888123456 → +359888123456
        }
        // Fallback: prepend +
        return "+" + p;
    }
}

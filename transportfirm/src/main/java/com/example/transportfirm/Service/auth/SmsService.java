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
     * Нормализира телефонни номера до E.164 формат (+359XXXXXXXXX).
     * Twilio изисква "+" префикс.
     */
    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String p = phone.replaceAll("[\\s\\-.()+]", "");
        if (p.isEmpty()) return null;

        if (p.startsWith("00")) {
            p = p.substring(2);  // 00359... → 359...
        }
        if (p.startsWith("359") && p.length() == 12) {
            return "+" + p;       // 359888123456 → +359888123456
        }
        if (p.startsWith("0") && p.length() == 10) {
            return "+359" + p.substring(1);  // 0888123456 → +359888123456
        }
        if (p.length() == 9 && !p.startsWith("0")) {
            return "+359" + p;    // 888123456 → +359888123456
        }
        // Международен номер — добавяме + ако липсва
        return "+" + p;
    }
}

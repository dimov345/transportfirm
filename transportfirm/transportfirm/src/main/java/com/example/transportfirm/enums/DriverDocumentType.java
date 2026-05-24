package com.example.transportfirm.enums;


public enum DriverDocumentType {

    // 🟦 Лични и идентификационни документи
    ID_CARD,                       // Лична карта (копие)
    PASSPORT,                      // Паспорт — ако се налага международно пътуване

    // 🟧 Задължителни документи за шофьор
    DRIVER_LICENSE,                // Шофьорска книжка
    QUALIFICATION_CARD,            // Квалификационна карта за професионална компетентност (ККТ)
    DIGITAL_TACHO_CARD,            // Дигитална тахо-карта
    PSYCHOLOGICAL_EXAM,            // Психологическа годност

    // 🟩 Фирмени документи
    EMPLOYMENT_CONTRACT,           // Трудов договор
    JOB_DESCRIPTION,               // Длъжностна характеристика
    SAFETY_INSTRUCTION,            // Инструктаж по безопасност
    GDPR_AGREEMENT,                // Съгласие за обработка на лични данни
    CONFIDENTIALITY_AGREEMENT,     // Споразумение за конфиденциалност

    // 🟨 Кариерни документи
    CV,                            // Автобиография
    DIPLOMA,                       // Диплома за образование
    TRAINING_CERTIFICATE,          // Сертификати от курсове / обучения

    // 🟪 Други
    OTHER                          // За други документи, които не са в списъка
}

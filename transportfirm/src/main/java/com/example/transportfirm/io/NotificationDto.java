package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    /** "vehicle" or "driver" */
    private String type;
    /** Human-readable document name (e.g. "Каско", "ГТП", "Шофьорска книжка") */
    private String documentType;
    /** Plate number or driver full name */
    private String entityName;
    /** UUID string used to build the navigation link */
    private String entityId;
    /** The actual expiry date */
    private LocalDate expiryDate;
    /** "expired" = past, "expiring" = within 30 days */
    private String status;
    /** Negative = already expired, positive = days remaining */
    private long daysUntilExpiry;
    /** Frontend route fragment, e.g. "/vehicles/{id}" */
    private String navigateTo;
}

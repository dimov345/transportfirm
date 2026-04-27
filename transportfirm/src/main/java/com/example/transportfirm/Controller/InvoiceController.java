package com.example.transportfirm.controller;

import com.example.transportfirm.io.InvoiceRequest;
import com.example.transportfirm.io.InvoiceResponse;
import com.example.transportfirm.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER', 'ACCOUNTANT')")
public class InvoiceController {

    private static final DateTimeFormatter BG_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final InvoiceService invoiceService;

    /** Генерира или връща съществуваща фактура за курса (идемпотентна). */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
    @PostMapping("/trip/{tripId}")
    public ResponseEntity<InvoiceResponse> createOrGetForTrip(
            @PathVariable UUID tripId,
            @RequestBody(required = false) InvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.createOrGetForTrip(tripId, req));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status) {
        return ResponseEntity.ok(invoiceService.getAll(page, size, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<InvoiceResponse> getByTripId(@PathVariable UUID tripId) {
        Optional<InvoiceResponse> inv = invoiceService.getByTripId(tripId);
        return inv.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable UUID id,
            @RequestBody InvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Връща HTML страница с фактурата — може да се отвори в нов таб и отпечата. */
    @GetMapping(value = "/{id}/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getInvoiceHtml(@PathVariable UUID id) {
        InvoiceResponse inv = invoiceService.getById(id);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(buildInvoiceHtml(inv));
    }

    // ── HTML invoice generation ───────────────────────────────────────────────

    private String buildInvoiceHtml(InvoiceResponse inv) {
        String sellerName    = inv.getSellerName()    != null ? esc(inv.getSellerName())    : "TransTrack ООД";
        String sellerVat     = inv.getSellerVatNumber()!= null ? esc(inv.getSellerVatNumber()): "—";
        String sellerAddress = inv.getSellerAddress() != null ? esc(inv.getSellerAddress()) : "—";
        String clientName    = inv.getClientName()    != null ? esc(inv.getClientName())    : "—";
        String clientVat     = inv.getClientVatNumber()!= null? esc(inv.getClientVatNumber()): "—";
        String clientAddress = inv.getClientAddress() != null ? esc(inv.getClientAddress()) : "—";
        String issueDate     = inv.getIssueDate()     != null ? inv.getIssueDate().format(BG_DATE)     : "—";
        String dueDate       = inv.getDueDate()       != null ? inv.getDueDate().format(BG_DATE)       : "—";
        String depDate       = inv.getTripDepartureDate() != null ? inv.getTripDepartureDate().format(BG_DATE) : "—";
        String arrDate       = inv.getTripArrivalDate()   != null ? inv.getTripArrivalDate().format(BG_DATE)  : "—";
        String route         = inv.getTripRoute()     != null ? esc(inv.getTripRoute())     : "—";
        String plate         = inv.getVehiclePlate()  != null ? esc(inv.getVehiclePlate())  : "—";
        String distance      = inv.getDistanceKm()    != null ? inv.getDistanceKm() + " км" : "—";
        String notes         = inv.getNotes()         != null ? esc(inv.getNotes())         : "";

        BigDecimal revenue  = nvl(inv.getRevenueEur());
        BigDecimal vat      = nvl(inv.getVatEur());
        BigDecimal tolls    = nvl(inv.getTollFeesEur());
        BigDecimal fuel     = nvl(inv.getFuelCostEur());
        BigDecimal border   = nvl(inv.getBorderFeesEur());
        BigDecimal parking  = nvl(inv.getParkingAccommodationEur());
        BigDecimal other    = nvl(inv.getOtherExpensesEur());
        BigDecimal expenses = nvl(inv.getTotalExpensesEur());
        BigDecimal netProfit = revenue.subtract(expenses);

        String statusLabel = switch (inv.getStatus()) {
            case "DRAFT"     -> "Чернова";
            case "ISSUED"    -> "Издадена";
            case "PAID"      -> "Платена";
            case "CANCELLED" -> "Анулирана";
            default          -> inv.getStatus();
        };
        String statusColor = switch (inv.getStatus()) {
            case "ISSUED"    -> "#1565c0";
            case "PAID"      -> "#2e7d32";
            case "CANCELLED" -> "#c62828";
            default          -> "#616161";
        };

        return """
<!DOCTYPE html>
<html lang="bg">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Фактура %s</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 13px; color: #212121; background: #f5f5f5; }
  .page { background: #fff; max-width: 820px; margin: 20px auto; padding: 40px; box-shadow: 0 2px 12px rgba(0,0,0,.15); }
  .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #1565c0; padding-bottom: 20px; margin-bottom: 24px; }
  .header-left h1 { font-size: 28px; color: #1565c0; letter-spacing: 2px; }
  .header-left .subtitle { color: #666; font-size: 12px; margin-top: 4px; }
  .header-right { text-align: right; }
  .inv-number { font-size: 20px; font-weight: 700; color: #212121; }
  .inv-meta { color: #555; font-size: 12px; margin-top: 4px; line-height: 1.8; }
  .status-badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; color: #fff; background: %s; margin-top: 6px; }
  .parties { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }
  .party { border: 1px solid #e0e0e0; border-radius: 6px; padding: 16px; }
  .party h3 { font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #666; margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 6px; }
  .party .name { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
  .party .detail { color: #555; font-size: 12px; line-height: 1.6; }
  .trip-info { background: #f8f9fa; border-left: 4px solid #1565c0; border-radius: 4px; padding: 12px 16px; margin-bottom: 24px; }
  .trip-info h3 { font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #666; margin-bottom: 8px; }
  .trip-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
  .trip-item label { font-size: 10px; text-transform: uppercase; color: #888; display: block; }
  .trip-item span  { font-size: 13px; font-weight: 500; }
  table.services { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
  table.services th { background: #1565c0; color: #fff; padding: 10px 12px; text-align: left; font-size: 12px; }
  table.services th:last-child, table.services td:last-child { text-align: right; }
  table.services td { padding: 10px 12px; border-bottom: 1px solid #eee; }
  table.services tr:last-child td { border-bottom: none; }
  .totals { margin-left: auto; width: 320px; border: 1px solid #e0e0e0; border-radius: 6px; overflow: hidden; margin-bottom: 24px; }
  .totals-row { display: flex; justify-content: space-between; padding: 8px 14px; font-size: 13px; border-bottom: 1px solid #eee; }
  .totals-row:last-child { border-bottom: none; background: #1565c0; color: #fff; font-weight: 700; font-size: 14px; }
  .totals-row.negative .val { color: #c62828; }
  .footer { border-top: 1px solid #e0e0e0; padding-top: 16px; color: #888; font-size: 11px; line-height: 1.7; }
  .notes-box { background: #fffde7; border-left: 4px solid #f9a825; border-radius: 4px; padding: 10px 14px; margin-bottom: 20px; font-size: 12px; color: #555; }
  .print-btn { display: block; margin: 0 auto 20px; padding: 10px 28px; background: #1565c0; color: #fff; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
  @media print {
    body { background: #fff; }
    .page { margin: 0; padding: 30px; box-shadow: none; }
    .print-btn { display: none; }
  }
</style>
</head>
<body>
<div class="page">
  <button class="print-btn" onclick="window.print()">🖨 Печат / Запази като PDF</button>

  <!-- Header -->
  <div class="header">
    <div class="header-left">
      <h1>ФАКТУРА</h1>
      <div class="subtitle">Транспортни услуги — TransTrack</div>
    </div>
    <div class="header-right">
      <div class="inv-number">%s</div>
      <div class="inv-meta">
        Дата на издаване: <strong>%s</strong><br>
        Краен срок: <strong>%s</strong>
      </div>
      <span class="status-badge">%s</span>
    </div>
  </div>

  <!-- Страни -->
  <div class="parties">
    <div class="party">
      <h3>Продавач</h3>
      <div class="name">%s</div>
      <div class="detail">
        ЕИК №: %s<br>
        Адрес: %s
      </div>
    </div>
    <div class="party">
      <h3>Купувач</h3>
      <div class="name">%s</div>
      <div class="detail">
        ЕИК №: %s<br>
        Адрес: %s
      </div>
    </div>
  </div>

  <!-- Информация за курса -->
  <div class="trip-info">
    <h3>Информация за курса</h3>
    <div class="trip-grid">
      <div class="trip-item"><label>Маршрут</label><span>%s</span></div>
      <div class="trip-item"><label>Камион</label><span>%s</span></div>
      <div class="trip-item"><label>Тръгване</label><span>%s</span></div>
      <div class="trip-item"><label>Пристигане</label><span>%s</span></div>
      <div class="trip-item"><label>Разстояние</label><span>%s</span></div>
    </div>
  </div>

  <!-- Услуги -->
  <table class="services">
    <thead>
      <tr>
        <th>Описание</th>
        <th>Ед. цена (€)</th>
        <th>Стойност (€)</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Транспортна услуга — %s</td>
        <td style="text-align:right">%s</td>
        <td>%s</td>
      </tr>
    </tbody>
  </table>

  <!-- Суми -->
  <div class="totals">
    <div class="totals-row"><span>Приход от транспорт</span><span class="val">€ %s</span></div>
    %s%s%s%s%s%s
    <div class="totals-row negative"><span>Разходи по курс</span><span class="val">− € %s</span></div>
    <div class="totals-row"><span>Нетна печалба</span><span class="val">€ %s</span></div>
  </div>

  %s

  <!-- Footer -->
  <div class="footer">
    <p>Фактура № %s &nbsp;|&nbsp; Издадена от <strong>%s</strong> &nbsp;|&nbsp; TransTrack система</p>
    <p>Документът е генериран автоматично и е валиден без подпис при електронно издаване.</p>
  </div>
</div>
</body>
</html>
""".formatted(
                inv.getInvoiceNumber(),           // title
                statusColor,                      // status badge color
                inv.getInvoiceNumber(),            // inv-number
                issueDate, dueDate, statusLabel,  // dates + status
                sellerName, sellerVat, sellerAddress,
                clientName, clientVat, clientAddress,
                route, plate, depDate, arrDate, distance,
                route,                             // service description
                fmt(revenue), fmt(revenue),        // unit price / total
                fmt(revenue),                      // revenue row
                expenseRow("ДДС", vat),
                expenseRow("Пътни такси", tolls),
                expenseRow("Гориво", fuel),
                expenseRow("Гранични такси", border),
                expenseRow("Паркинг/нощувки", parking),
                expenseRow("Други разходи", other),
                fmt(expenses),                    // total expenses
                fmt(netProfit),                   // net profit
                notes.isEmpty() ? "" : "<div class=\"notes-box\"><strong>Бележки:</strong> " + notes + "</div>",
                inv.getInvoiceNumber(), sellerName
        );
    }

    private String expenseRow(String label, BigDecimal val) {
        if (val == null || val.compareTo(BigDecimal.ZERO) == 0) return "";
        return "<div class=\"totals-row\"><span>" + label + "</span><span class=\"val\">€ " + fmt(val) + "</span></div>";
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return String.format("%.2f", val);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}

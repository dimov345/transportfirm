import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Invoice, InvoiceRequest, InvoicePage } from '../models/invoice.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly apiUrl = environment.apiUrl + '/invoices';

  constructor(private http: HttpClient) {}

  /** Генерира или връща съществуваща фактура за курс (идемпотентна). */
  createOrGetForTrip(tripId: string, req: InvoiceRequest = {}): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/trip/${tripId}`, req);
  }

  getAll(page = 0, size = 20, status?: string): Observable<InvoicePage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<InvoicePage>(this.apiUrl, { params });
  }

  getById(id: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`);
  }

  getByTripId(tripId: string): Observable<Invoice | null> {
    return this.http.get<Invoice>(`${this.apiUrl}/trip/${tripId}`);
  }

  update(id: string, req: InvoiceRequest): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Зарежда HTML на фактурата чрез HTTP (с JWT auth header)
   * и я отваря в нов таб чрез Blob URL.
   */
  openInvoiceHtml(id: string): Observable<void> {
    return this.http.get(`${this.apiUrl}/${id}/html`, { responseType: 'text' }).pipe(
      map(html => {
        const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
        const url  = URL.createObjectURL(blob);
        const win  = window.open(url, '_blank');
        // Освобождаване на обекта след малко — браузърът вече е заредил HTML-а
        setTimeout(() => URL.revokeObjectURL(url), 5000);
        if (!win) {
          throw new Error('Браузърът блокира новия таб. Моля разрешете pop-up за този сайт.');
        }
      })
    );
  }
}

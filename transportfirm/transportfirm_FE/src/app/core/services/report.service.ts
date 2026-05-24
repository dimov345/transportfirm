import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AccountingDashboard } from '../models/accounting.model';

export type ReportType   = 'salary' | 'trips' | 'maintenance' | 'combined';
export type ReportPeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly apiUrl     = environment.apiUrl + '/reports/download';
  private readonly dataApiUrl = environment.apiUrl + '/reports/data';
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser  = isPlatformBrowser(this.platformId);

  constructor(private http: HttpClient) {}

  download(
    type:    ReportType,
    period:  ReportPeriod,
    year:    number,
    month?:  number,
    week?:   number
  ): Observable<void> {
    let params = new HttpParams()
      .set('type',   type)
      .set('period', period)
      .set('year',   year.toString());
    if (month != null) params = params.set('month', month.toString());
    if (week  != null) params = params.set('week',  week.toString());

    return this.http.get(this.apiUrl, { params, responseType: 'blob' }).pipe(
      tap((blob: Blob) => {
        if (!this.isBrowser) return;
        const url = URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href     = url;
        a.download = `${type}_report.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }),
      map(() => void 0)
    );
  }

  getData(
    period: ReportPeriod,
    year:   number,
    month?: number,
    week?:  number
  ): Observable<AccountingDashboard> {
    let params = new HttpParams()
      .set('period', period)
      .set('year',   year.toString());
    if (month != null) params = params.set('month', month.toString());
    if (week  != null) params = params.set('week',  week.toString());

    return this.http.get<AccountingDashboard>(this.dataApiUrl, { params });
  }
}

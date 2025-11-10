import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DriverInfo {
  egn: string;
  name: string;
  phone: string;
  email: string;
  driverLicenseIssuedOn: string;
  driverLicenseExpiresOn: string;
  qualificationCardIssuedOn: string;
  qualificationCardExpiresOn: string;
  psychologicalExamIssuedOn?: string;
  psychologicalExamExpiresOn?: string;
  digitalCardIssuedOn?: string;
  digitalCardExpiresOn?: string;
}

export interface DriverDocument {
  id: number;
  egn: string;
  fileName: string;
}

@Injectable({ providedIn: 'root' })
export class DriverService {
  private apiUrl = 'http://localhost:8080/api/drivers';

  constructor(private http: HttpClient) {}

  getAll(): Observable<DriverInfo[]> {
    return this.http.get<DriverInfo[]>(this.apiUrl);
  }

  getByEgn(egn: string): Observable<DriverInfo> {
    return this.http.get<DriverInfo>(`${this.apiUrl}/${egn}`);
  }

  create(driver: DriverInfo): Observable<DriverInfo> {
    return this.http.post<DriverInfo>(this.apiUrl, driver);
  }

  update(egn: string, driver: DriverInfo): Observable<DriverInfo> {
    return this.http.put<DriverInfo>(`${this.apiUrl}/${egn}`, driver);
  }

  delete(egn: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${egn}`);
  }

  // ===== Documents =====
  getDocuments(egn: string): Observable<DriverDocument[]> {
    return this.http.get<DriverDocument[]>(`${this.apiUrl}/${egn}/documents`);
  }

  uploadDocument(egn: string, file: File): Observable<DriverDocument> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DriverDocument>(`${this.apiUrl}/${egn}/documents`, formData);
  }

  downloadDocument(egn: string, id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${egn}/documents/${id}`, { responseType: 'blob' });
  }

  deleteDocument(egn: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${egn}/documents/${id}`);
  }
}

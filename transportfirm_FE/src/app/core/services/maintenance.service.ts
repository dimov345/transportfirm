import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type MaintenanceType = 'REPAIR' | 'SERVICE' | 'INSPECTION' | 'TIRE_CHANGE' | 'BREAKDOWN' | 'ACCIDENT' | 'OTHER';
export type MaintenanceStatus = 'OPEN' | 'CLOSED' | 'CANCELED';
export type MaintenanceDocumentType = 'INVOICE' | 'PHOTO' | 'REPORT';

export interface MaintenanceRecord {
  id: string;
  vehicle: { id: string; plateNumber: string; model: string };
  type: MaintenanceType;
  status: MaintenanceStatus;
  openedAt: string;
  closedAt?: string;
  odometerKmAtService?: number;
  description?: string;
  workshopName?: string;
  invoiceNumber?: string;
  invoiceDate?: string;
  partsNet?: number;
  partsVat?: number;
  otherNet?: number;
  otherVat?: number;
  totalNet?: number;
  totalVat?: number;
  totalGross?: number;
}

export interface MaintenanceDocument {
  id: string;
  fileName: string;
  filePath: string;
  docType?: MaintenanceDocumentType;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private apiUrl = 'http://localhost:8080/api/maintenance';

  constructor(private http: HttpClient) {}

  getByVehicle(vehicleId: string, page = 0, size = 20): Observable<PageResponse<MaintenanceRecord>> {
    return this.http.get<PageResponse<MaintenanceRecord>>(
      `${this.apiUrl}/vehicle/${vehicleId}`,
      { params: { page: page.toString(), size: size.toString() } }
    );
  }

  getById(recordId: string): Observable<MaintenanceRecord> {
    return this.http.get<MaintenanceRecord>(`${this.apiUrl}/${recordId}`);
  }

  create(vehicleId: string, type: MaintenanceType): Observable<MaintenanceRecord> {
    return this.http.post<MaintenanceRecord>(
      `${this.apiUrl}/create/${vehicleId}`,
      null,
      { params: { type } }
    );
  }

  update(recordId: string, data: Partial<MaintenanceRecord>): Observable<MaintenanceRecord> {
    return this.http.put<MaintenanceRecord>(`${this.apiUrl}/${recordId}`, data);
  }

  close(recordId: string): Observable<MaintenanceRecord> {
    return this.http.put<MaintenanceRecord>(`${this.apiUrl}/${recordId}/close`, null);
  }

  getDocuments(recordId: string): Observable<MaintenanceDocument[]> {
    return this.http.get<MaintenanceDocument[]>(`${this.apiUrl}/${recordId}/documents`);
  }

  uploadDocument(recordId: string, file: File, docType?: MaintenanceDocumentType): Observable<MaintenanceDocument> {
    const formData = new FormData();
    formData.append('file', file);
    if (docType) formData.append('docType', docType);
    return this.http.post<MaintenanceDocument>(`${this.apiUrl}/${recordId}/documents/upload`, formData);
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documents/${documentId}/download`, { responseType: 'blob' });
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${documentId}`);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmployeeDocument } from '../models/employee/employee-document.model';

@Injectable({ providedIn: 'root' })
export class EmployeeDocumentService {
  // ако при теб е без /api -> смени URL-то
  private apiUrl = 'http://localhost:8080/api/employee-documents';

  constructor(private http: HttpClient) {}

  getDocuments(employeeId: string): Observable<EmployeeDocument[]> {
    return this.http.get<EmployeeDocument[]>(`${this.apiUrl}/${employeeId}`);
  }

  uploadDocument(employeeId: string, type: string, file: File): Observable<EmployeeDocument> {
    const formData = new FormData();
    formData.append('file', file);

    // backend: POST /upload/{employeeId}/{type}
    return this.http.post<EmployeeDocument>(`${this.apiUrl}/upload/${employeeId}/${type}`, formData);
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${documentId}`, {
      responseType: 'blob'
    });
  }

  deleteDocument(documentId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${documentId}`);
  }
}

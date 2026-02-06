import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee/employee.model';
import { DriverInfo } from '../models/driver/driver-info.model';
import { DriverDocument } from '../models/driver/driver-document.model';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private apiUrl = 'http://localhost:8080/api/drivers';

  constructor(private http: HttpClient) {}

  // ✅ всички служители с jobTitle=DRIVER
  getAllDriversFromEmployee(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/job/DRIVER`);
  }

  // ✅ Employee с вътрешен driverInfo
  getEmployee(employeeId: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/employee/${employeeId}`);
  }

  // ✅ UPDATE DriverInfo (по driverInfoId)
  updateDriverInfo(driverInfoId: string, payload: Partial<DriverInfo>): Observable<DriverInfo> {
    return this.http.put<DriverInfo>(`${this.apiUrl}/${driverInfoId}`, payload);
  }

  // ✅ DELETE DriverInfo (по driverInfoId)
  delete(driverInfoId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${driverInfoId}`);
  }

  // ===== Документи (по Employee.id) =====
  getDocuments(employeeId: string): Observable<DriverDocument[]> {
    return this.http.get<DriverDocument[]>(`${this.apiUrl}/employee/${employeeId}/documents`);
  }

  uploadDocument(employeeId: string, type: string, file: File): Observable<DriverDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<DriverDocument>(`${this.apiUrl}/employee/${employeeId}/documents`, formData);
  }

  downloadDocument(docId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documents/${docId}/download`, { responseType: 'blob' });
  }

  deleteDocument(docId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${docId}`);
  }
}

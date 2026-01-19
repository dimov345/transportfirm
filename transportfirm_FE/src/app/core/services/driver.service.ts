import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Employee {
  id: number;
  egn: string;
  name: string;
  phone: string;
  email: string;
  jobTitle?: string;
}

export interface DriverInfo {
  id: number;
  employee?: Employee | null;

  driverLicenseExpiresOn: string;
  qualificationCardExpiresOn: string;
  psychologicalExamExpiresOn: string;
  digitalCardExpiresOn: string;

  statusClass: string;
  statusTooltip: string;
}

export interface DriverDocument {
  id: number;
  fileName: string;
  filePath: string;
  driver: {
    id: number;
  };
  selectedType: string;
}


@Injectable({ providedIn: 'root' })
export class DriverService {
  private apiUrl = 'http://localhost:8080/api/drivers';
  private apiEmployeesUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  // 1. Взимаме ВСИЧКИ шофьори от Employee (дори без DriverInfo)
  getAllDriversFromEmployee(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/job/DRIVER`);
  }

  getDriverFullInfo(employeeId: number): Observable<DriverInfo> {
    return this.http.get<DriverInfo>(`${this.apiUrl}/${employeeId}/full-info`);
  }

  // 2. Взимаме DriverInfo само ако съществува
  getDriverById(id: number): Observable<DriverInfo> {
    return this.http.get<DriverInfo>(`${this.apiUrl}/${id}`);
  }

  // 3. Създаване DriverInfo
  create(driver: DriverInfo): Observable<DriverInfo> {
    return this.http.post<DriverInfo>(this.apiUrl, driver);
  }

  // 4. Update по ID (не по egn)
  update(id: number, driver: DriverInfo) {
    return this.http.put<DriverInfo>(`${this.apiUrl}/${id}`, driver);
  }

  // 5. Delete по ID
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ===== Документи =====
  getDocuments(driverInfoId: number) {
    return this.http.get<DriverDocument[]>(`${this.apiUrl}/${driverInfoId}/documents`);
  }

  uploadDocument(driverInfoId: number, type: string, file: File) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return this.http.post(`${this.apiUrl}/${driverInfoId}/documents`, formData);
  }

  downloadDocument(docId: number) {
    return this.http.get(`${this.apiUrl}/documents/${docId}/download`, {
      responseType: 'blob'
    });
  }

  deleteDocument(docId: number) {
    return this.http.delete(`${this.apiUrl}/documents/${docId}`);
  }

  exportDriversCsv(params: any) {
  return this.http.get<{ fileName: string, count: number }>(
    `${this.apiUrl}/export`,
    { params }
  );
}

}

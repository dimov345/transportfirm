import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// --- МИНИМАЛНИ ДАННИ ЗА ЛИСТА ---
export interface EmployeeListItem {
  id: number;
  egn: string;
  name: string;
  jobTitle: string;
  phone: string;
  email: string;
}

// --- ПЪЛЕН EMPLOYEE ОТ BACKEND ---
export interface Employee {
  id: number;
  egn: string;
  name: string;
  dateOfBirth?: string;
  phone: string;
  email: string;

  addressPermanent?: string;
  addressCurrent?: string;

  jobTitle: string;
  hiredDate: string;
  employmentStatus: string;
  firedDate?: string;
  contractType?: string;

  salary?: number;
  salaryNeto?: number;
  salaryCurrency?: string;
  workingHours?: string;

  bankName?: string;
  iban?: string;

  username?: string;
  password?: string;
  role?: string;

  documents: EmployeeDocument[];
}

// --- PAYLOAD ЗА CREATE / UPDATE ---
export interface EmployeePayload {
  egn: string;
  name: string;
  dateOfBirth?: string;

  phone: string;
  email: string;

  addressPermanent?: string;
  addressCurrent?: string;

  jobTitle: string;
  hiredDate: string;
  employmentStatus?: string;
  firedDate?: string;

  contractType?: string;
  salary?: number;
  salaryNeto?: number;
  salaryCurrency?: string;
  workingHours?: string;

  bankName?: string;
  iban?: string;

  username?: string;
  password?: string;
  role?: string;
}

// --- DOCUMENTS ---
export interface EmployeeDocument {
  id: number;
  type: string;
  fileName: string;
  filePath: string;
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private apiUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  getAll(): Observable<EmployeeListItem[]> {
    return this.http.get<EmployeeListItem[]>(this.apiUrl);
  }

  getDrivers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/job/DRIVER`);
  }

  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  create(payload: EmployeePayload): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, payload);
  }

  update(id: number, payload: EmployeePayload): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // DOCUMENTS
  getDocuments(employeeId: number): Observable<EmployeeDocument[]> {
    return this.http.get<EmployeeDocument[]>(`${this.apiUrl}/${employeeId}/documents`);
  }

  uploadDocument(employeeId: number, file: File): Observable<EmployeeDocument> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<EmployeeDocument>(`${this.apiUrl}/${employeeId}/documents`, form);
  }

  deleteDocument(employeeId: number, documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${employeeId}/documents/${documentId}`);
  }
}

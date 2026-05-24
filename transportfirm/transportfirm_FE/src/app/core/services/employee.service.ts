import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// --- МИНИМАЛНИ ДАННИ ЗА ЛИСТА ---
export interface EmployeeListItem {
  id: string;
  egn: string;
  name: string;
  jobTitle: string;
  phone: string;
  email: string;
}

// --- ПЪЛЕН EMPLOYEE ОТ BACKEND ---
export interface Employee {
  id: string;
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
  id: string;
  type: string;
  fileName: string;
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly apiUrl = environment.apiUrl + '/employees';

  constructor(private http: HttpClient) {}

  getAll(): Observable<EmployeeListItem[]> {
    return this.http.get<EmployeeListItem[]>(this.apiUrl);
  }

  getDrivers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/job/DRIVER`);
  }

  /** Returns the currently logged-in user's own Employee record (all roles). */
  getMe(): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/me`);
  }

  getById(id: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  create(payload: EmployeePayload): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, payload);
  }

  update(id: string, payload: EmployeePayload): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/update/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}

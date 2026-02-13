import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee } from '../models/employee/employee.model';

export interface CreateEmployeeRequest {
  // лични
  egn: string;
  name: string;
  dateOfBirth: string; // yyyy-MM-dd
  phone: string;
  email: string;

  // адреси
  addressPermanent?: string;
  addressCurrent?: string;

  // фирмени
  jobTitle: JobTitle;
  role: Role;
  hiredDate: string; // yyyy-MM-dd
  contractType: ContractType;
  salary: number;
  salaryNeto?: number;
  salaryCurrency: string;
  workingHours: string;

  // банкови
  bankName: string;
  iban: string;

  // auth
  username: string;
}

export enum JobTitle {
  DRIVER = 'DRIVER',
  MECHANIC = 'MECHANIC',
  DISPATCHER = 'DISPATCHER',
  ACCOUNTANT = 'ACCOUNTANT',
  MANAGER = 'MANAGER',
  HR = 'HR',
  ADMINISTRATOR = 'ADMINISTRATOR'
}

export enum Role {
  ADMIN = 'ADMIN',
  MANAGER = 'MANAGER',
  DISPATCHER = 'DISPATCHER',
  ACCOUNTANT = 'ACCOUNTANT',
  DRIVER = 'DRIVER',
  MECHANIC = 'MECHANIC'
}

export enum ContractType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  TEMPORARY = 'TEMPORARY',
  INTERNSHIP = 'INTERNSHIP',
  CIVIL_CONTRACT = 'CIVIL_CONTRACT'
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  private readonly baseUrl = 'http://localhost:8080/api';

  createEmployee(payload: CreateEmployeeRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/admin/create-employee`, payload);
  }

  getEmployeeById(id: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/employees/${id}`);
  }

  updateEmployee(id: string, payload: Partial<Employee>): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/employees/update/${id}`, payload);
  }
}

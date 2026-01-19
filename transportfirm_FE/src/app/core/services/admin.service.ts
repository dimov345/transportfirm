import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';



export interface CreateEmployeeRequest {

  // лични
  egn: string;
  name: string;
  dateOfBirth: string; // ISO format: yyyy-MM-dd
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



@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private readonly API_URL = 'http://localhost:8080/api/auth/admin';

  constructor(private http: HttpClient) {}

  createEmployee(request: CreateEmployeeRequest): Observable<void> {
    return this.http.post<void>(
      `${this.API_URL}/create-employee`,
      request
    );
  }
}

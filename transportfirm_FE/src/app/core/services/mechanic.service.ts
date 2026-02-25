import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee/employee.model';
import { MechanicInfo } from '../models/mechanic/mechanic-info.model';
import { TruckGroup } from './truck-group.service';
import { EmployeeDocument } from '../models/employee/employee-document.model';

@Injectable({ providedIn: 'root' })
export class MechanicService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAllMechanics(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/employees/job/MECHANIC`);
  }

  getEmployee(employeeId: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/employees/${employeeId}`);
  }

  getMechanicById(mechanicInfoId: string): Observable<MechanicInfo> {
    return this.http.get<MechanicInfo>(`${this.apiUrl}/mechanics/${mechanicInfoId}`);
  }

  getGroupsByMechanic(mechanicInfoId: string): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/mechanics/${mechanicInfoId}/groups`);
  }

  createMechanic(employeeId: string): Observable<MechanicInfo> {
    return this.http.post<MechanicInfo>(`${this.apiUrl}/mechanics/create/${employeeId}`, null);
  }

  deleteMechanic(mechanicInfoId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/mechanics/${mechanicInfoId}`);
  }

  // Документи (общи employee документи)
  getDocuments(employeeId: string): Observable<EmployeeDocument[]> {
    return this.http.get<EmployeeDocument[]>(`${this.apiUrl}/employee-documents/${employeeId}`);
  }

  uploadDocument(employeeId: string, type: string, file: File): Observable<EmployeeDocument> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<EmployeeDocument>(
      `${this.apiUrl}/employee-documents/upload/${employeeId}/${type}`,
      formData
    );
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/employee-documents/download/${documentId}`, {
      responseType: 'blob'
    });
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/employee-documents/delete/${documentId}`);
  }
}

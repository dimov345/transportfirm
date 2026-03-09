import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee/employee.model';
import { DispatcherInfo } from '../models/dispatcher/dispatcher-info.model';
import { TruckGroup } from './truck-group.service';

import { EmployeeDocument } from '../models/employee/employee-document.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DispatcherService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllDispatchers(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/employees/job/DISPATCHER`);
  }

  getEmployee(employeeId: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/employees/${employeeId}`);
  }

  getGroupsByDispatcher(dispatcherInfoId: string): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/dispatchers/${dispatcherInfoId}/groups`);
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

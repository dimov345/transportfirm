import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee/employee.model';
import { DispatcherInfo } from '../models/dispatcher/dispatcher-info.model';
import { DispatcherDocument } from '../models/dispatcher/dispatcher-document.model';
import { TruckGroup } from './truck-group.service';
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

  // Документи (dispatcher_documents)
  getDocuments(employeeId: string): Observable<DispatcherDocument[]> {
    return this.http.get<DispatcherDocument[]>(`${this.apiUrl}/dispatchers/employee/${employeeId}/documents`);
  }

  uploadDocument(employeeId: string, type: string, file: File): Observable<DispatcherDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<DispatcherDocument>(
      `${this.apiUrl}/dispatchers/employee/${employeeId}/documents`,
      formData
    );
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/dispatchers/documents/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/dispatchers/documents/${documentId}`);
  }
}

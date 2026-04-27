import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee/employee.model';
import { MechanicInfo } from '../models/mechanic/mechanic-info.model';
import { MechanicDocument } from '../models/mechanic/mechanic-document.model';
import { TruckGroup } from './truck-group.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MechanicService {
  private readonly apiUrl = environment.apiUrl;

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

  // Документи (mechanic_documents)
  getDocuments(employeeId: string): Observable<MechanicDocument[]> {
    return this.http.get<MechanicDocument[]>(`${this.apiUrl}/mechanics/employee/${employeeId}/documents`);
  }

  uploadDocument(employeeId: string, type: string, file: File): Observable<MechanicDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<MechanicDocument>(
      `${this.apiUrl}/mechanics/employee/${employeeId}/documents`,
      formData
    );
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/mechanics/documents/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/mechanics/documents/${documentId}`);
  }
}

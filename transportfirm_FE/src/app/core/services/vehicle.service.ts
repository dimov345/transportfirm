import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VehicleInfo {
  id: string;
  plateNumber: string;
  model: string;
  engineNumber: string;
  chassisNumber: string;
  yearOfManufacture: number;
  owner: string;
  typePps: string;
  technicalCondition: string;
  standardEmissions: string;
  kaskoOt?: string;
  kaskoDo?: string;
  grazhdanskaOtgovornostOt?: string;
  grazhdanskaOtgovornostDo?: string;
  gtpOt?: string;
  gtpDo?: string;
  vinetkaOt?: string;
  vinetkaDo?: string;

  // ===== New model fields (backend VehicleRecord) =====
  vehicleStatus?: string;
  odometerKm?: number;
  lastServiceOdometerKm?: number;
  lastServiceDate?: string;
  nextServiceDueOdometerKm?: number;
  nextServiceDueDate?: string;
  avgConsumptionLper100?: number;
  notesForMechanic?: string;

  // Leasing
  leased?: boolean;
  leasingStartDate?: string;
  leasingEndDate?: string;
  leasingCompany?: string;
  leasingContractNumber?: string;

  // Assignments / groups (optional; used mainly in details screens)
  dispatcherGroup?: { id: string; groupName?: string } | null;
  mechanicGroup?: { id: string; groupName?: string } | null;
  driver?: any | null;
}

export interface VehicleDocument {
  id: string;
  type: string;
  fileName: string;
}

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private apiUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) {}

  // ===== Vehicle CRUD =====
  getAll(): Observable<VehicleInfo[]> {
    return this.http.get<VehicleInfo[]>(this.apiUrl);
  }

  getByGroup(groupId: string): Observable<VehicleInfo[]> {
    return this.http.get<VehicleInfo[]>(`${this.apiUrl}/group/${groupId}`);
  }

  getById(id: string): Observable<VehicleInfo> {
    return this.http.get<VehicleInfo>(`${this.apiUrl}/${id}`);
  }

  create(vehicle: VehicleInfo): Observable<VehicleInfo> {
    return this.http.post<VehicleInfo>(this.apiUrl, vehicle);
  }

  update(id: string, vehicle: VehicleInfo): Observable<VehicleInfo> {
    return this.http.put<VehicleInfo>(`${this.apiUrl}/${id}`, vehicle);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ===== Documents =====
  getDocuments(id: string): Observable<VehicleDocument[]> {
    return this.http.get<VehicleDocument[]>(`${this.apiUrl}/${id}/documents`);
  }

  uploadDocument(id: string, type: string, file: File): Observable<VehicleDocument> {
    const formData = new FormData();
    formData.append('type', type);
    formData.append('file', file);
    return this.http.post<VehicleDocument>(`${this.apiUrl}/${id}/documents`, formData);
  }

  downloadDocument(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documents/${id}/download`, { responseType: 'blob' });
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${id}`);
  }
}

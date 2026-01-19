import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VehicleInfo {
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
}

export interface VehicleDocument {
  id: number;
  fileName: string;
  filepath: string;
  vehicle: {
    plateNumber: string;
  };
}

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private apiUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) {}

  // ===== Vehicle CRUD =====
  getAll(): Observable<VehicleInfo[]> {
    return this.http.get<VehicleInfo[]>(this.apiUrl);
  }

  getByPlateNumber(plateNumber: string): Observable<VehicleInfo> {
    return this.http.get<VehicleInfo>(`${this.apiUrl}/${plateNumber}`);
  }

  create(vehicle: VehicleInfo): Observable<VehicleInfo> {
    return this.http.post<VehicleInfo>(this.apiUrl, vehicle);
  }

  update(plateNumber: string, vehicle: VehicleInfo): Observable<VehicleInfo> {
    return this.http.put<VehicleInfo>(`${this.apiUrl}/${plateNumber}`, vehicle);
  }

  delete(plateNumber: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${plateNumber}`);
  }

  // ===== Documents =====
  getDocuments(plateNumber: string): Observable<VehicleDocument[]> {
    return this.http.get<VehicleDocument[]>(`${this.apiUrl}/${plateNumber}/documents`);
  }

  uploadDocument(plateNumber: string, file: File): Observable<VehicleDocument> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehicleDocument>(`${this.apiUrl}/${plateNumber}/documents`, formData);
  }

  downloadDocument(plateNumber: string, id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${plateNumber}/documents/${id}`, { responseType: 'blob' });
  }

  deleteDocument(plateNumber: string, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${plateNumber}/documents/${id}`);
  }
}
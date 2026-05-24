import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FreightTrip } from '../models/freight-trip.model';

export interface FreightTripPage {
  content: FreightTrip[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class FreightTripService {
  private readonly apiUrl = environment.apiUrl + '/freight-trips';

  constructor(private http: HttpClient) {}

  getPaged(search: string, status: string, vehicleId: string, page: number, size: number): Observable<FreightTripPage> {
    let params = new HttpParams()
      .set('search', search)
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) params = params.set('status', status);
    if (vehicleId) params = params.set('vehicleId', vehicleId);
    return this.http.get<FreightTripPage>(this.apiUrl, { params });
  }

  getAll(): Observable<FreightTrip[]> {
    return this.http.get<FreightTrip[]>(`${this.apiUrl}/all`);
  }

  getById(id: string): Observable<FreightTrip> {
    return this.http.get<FreightTrip>(`${this.apiUrl}/${id}`);
  }

  getByVehicleId(vehicleId: string): Observable<FreightTrip[]> {
    return this.http.get<FreightTrip[]>(`${this.apiUrl}/vehicle/${vehicleId}`);
  }

  create(trip: FreightTrip, vehicleId: string): Observable<FreightTrip> {
    const params = new HttpParams().set('vehicleId', vehicleId);
    return this.http.post<FreightTrip>(this.apiUrl, trip, { params });
  }

  update(id: string, trip: FreightTrip, vehicleId?: string): Observable<FreightTrip> {
    let params = new HttpParams();
    if (vehicleId) params = params.set('vehicleId', vehicleId);
    return this.http.put<FreightTrip>(`${this.apiUrl}/${id}`, trip, { params });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

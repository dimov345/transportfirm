import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DriverInfo } from '../models/driver/driver-info.model';

import { VehicleInfo } from './vehicle.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private readonly apiUrl = environment.apiUrl + '/assignments';

  constructor(private http: HttpClient) {}

  // PUT /api/assignments/drivers/{driverInfoId}/vehicle/{vehicleId}
  assignVehicleToDriver(driverInfoId: string, vehicleId: string): Observable<DriverInfo> {
    return this.http.put<DriverInfo>(`${this.apiUrl}/drivers/${driverInfoId}/vehicle/${vehicleId}`, {});
  }

  // DELETE /api/assignments/drivers/{driverInfoId}/vehicle
  unassignVehicleFromDriver(driverInfoId: string): Observable<DriverInfo> {
    return this.http.delete<DriverInfo>(`${this.apiUrl}/drivers/${driverInfoId}/vehicle`);
  }

  // GET /api/assignments/drivers/{driverInfoId}/vehicle
  getVehicleOfDriver(driverInfoId: string): Observable<VehicleInfo> {
    return this.http.get<VehicleInfo>(`${this.apiUrl}/drivers/${driverInfoId}/vehicle`);
  }

  // GET /api/assignments/vehicles/{vehicleId}/driver
  getDriverOfVehicle(vehicleId: string): Observable<DriverInfo> {
    return this.http.get<DriverInfo>(`${this.apiUrl}/vehicles/${vehicleId}/driver`);
  }

  // GET /api/assignments/vehicles/available  (ако добавиш бекенд endpoint-а)
  getAvailableVehicles(): Observable<VehicleInfo[]> {
    return this.http.get<VehicleInfo[]>(`${this.apiUrl}/vehicles/available`);
  }
}

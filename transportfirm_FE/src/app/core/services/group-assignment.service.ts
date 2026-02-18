import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GroupType } from './truck-group.service';

@Injectable({ providedIn: 'root' })
export class GroupAssignmentService {
  private apiUrl = 'http://localhost:8080/api/assignments';

  constructor(private http: HttpClient) {}

  assignVehicle(groupType: GroupType, groupId: string, vehicleId: string): Observable<void> {
    const prefix = groupType === 'MECHANIC' ? 'mechanic-group' : 'dispatcher-group';
    return this.http.put<void>(`${this.apiUrl}/${prefix}/${groupId}/vehicle/${vehicleId}`, {}, { withCredentials: true });
  }

  removeVehicle(groupType: GroupType, groupId: string, vehicleId: string): Observable<void> {
    const prefix = groupType === 'MECHANIC' ? 'mechanic-group' : 'dispatcher-group';
    return this.http.delete<void>(`${this.apiUrl}/${prefix}/${groupId}/vehicle/${vehicleId}`, { withCredentials: true });
  }
}

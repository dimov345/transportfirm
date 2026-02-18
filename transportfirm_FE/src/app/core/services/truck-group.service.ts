import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type GroupType = 'MECHANIC' | 'DISPATCHER';

export interface TruckGroup {
  id: string;
  groupName: string;
  groupType: GroupType;

  // backend може да връща owner info (не е задължително)
  mechanic?: { id: string } | null;
  dispatcher?: { id: string } | null;
}

@Injectable({ providedIn: 'root' })
export class TruckGroupService {
  // важно: следвам твоя pattern с /api
  private apiUrl = 'http://localhost:8080/api/auth/admin';

  constructor(private http: HttpClient) {}

  createMechanicGroup(mechanicId: string, groupName: string): Observable<TruckGroup> {
    const url = `${this.apiUrl}/groups/mechanic/${mechanicId}`;
    return this.http.post<TruckGroup>(url, null, {
      params: { name: groupName },
      withCredentials: true
    });
  }

  createDispatcherGroup(dispatcherId: string, groupName: string): Observable<TruckGroup> {
    const url = `${this.apiUrl}/groups/dispatcher/${dispatcherId}`;
    return this.http.post<TruckGroup>(url, null, {
      params: { name: groupName },
      withCredentials: true
    });
  }

  getGroupsByMechanic(mechanicId: string) {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/mechanic/${mechanicId}`);
  }

  getGroupsByDispatcher(dispatcherId: string) {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/dispatcher/${dispatcherId}`);
  }

}

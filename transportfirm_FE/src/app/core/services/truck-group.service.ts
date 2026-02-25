import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type GroupType = 'MECHANIC' | 'DISPATCHER';

export interface TruckGroup {
  id: string;
  groupName: string;
  groupType: GroupType;
  mechanic?: { id: string } | null;
  dispatcher?: { id: string } | null;
}

@Injectable({ providedIn: 'root' })
export class TruckGroupService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Групите на логнатия механик (от JWT Principal)
  getGroupsByMechanicMe(): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/mechanics/me/groups`);
  }

  // Групите на логнатия спедитор (от JWT Principal)
  getGroupsByDispatcherMe(): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/dispatchers/me/groups`);
  }

  // Групите на конкретен механик (по mechanicInfoId) — за admin view
  getGroupsByMechanic(mechanicInfoId: string): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/mechanics/${mechanicInfoId}/groups`);
  }

  // Групите на конкретен спедитор (по dispatcherInfoId) — за admin view
  getGroupsByDispatcher(dispatcherInfoId: string): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/dispatchers/${dispatcherInfoId}/groups`);
  }

  // Създаване на група за механик (за employee-details)
  createMechanicGroup(mechanicId: string, groupName: string): Observable<TruckGroup> {
    return this.http.post<TruckGroup>(
      `${this.apiUrl}/auth/admin/groups/mechanic/${mechanicId}`,
      null,
      { params: { name: groupName } }
    );
  }

  // Създаване на група за спедитор (за employee-details)
  createDispatcherGroup(dispatcherId: string, groupName: string): Observable<TruckGroup> {
    return this.http.post<TruckGroup>(
      `${this.apiUrl}/auth/admin/groups/dispatcher/${dispatcherId}`,
      null,
      { params: { name: groupName } }
    );
  }

  getAll(): Observable<TruckGroup[]> {
    return this.http.get<TruckGroup[]>(`${this.apiUrl}/truck-groups`);
  }

  create(groupName: string, groupType: GroupType): Observable<TruckGroup> {
    return this.http.post<TruckGroup>(`${this.apiUrl}/truck-groups`, { groupName, groupType });
  }

  update(id: string, groupName: string): Observable<TruckGroup> {
    return this.http.put<TruckGroup>(`${this.apiUrl}/truck-groups/${id}`, { groupName });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/truck-groups/${id}`);
  }
}

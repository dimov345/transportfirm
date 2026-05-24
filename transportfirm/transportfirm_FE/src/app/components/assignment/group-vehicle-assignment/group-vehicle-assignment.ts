import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { VehicleInfo, VehicleService } from '../../../core/services/vehicle.service';
import { GroupAssignmentService } from '../../../core/services/group-assignment.service';
import { GroupType } from '../../../core/services/truck-group.service';

@Component({
  selector: 'app-group-vehicle-assignment',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './group-vehicle-assignment.html',
  styleUrls: ['./group-vehicle-assignment.scss']
})
export class GroupVehicleAssignment implements OnInit {
  @Input() groupId!: string;
  @Input() groupType!: GroupType; // 'MECHANIC' | 'DISPATCHER'

  loading = true;
  saving = false;

  error = '';
  success = '';

  allVehicles: VehicleInfo[] = [];
  vehiclesInGroup: VehicleInfo[] = [];
  availableVehicles: VehicleInfo[] = [];
  selectedVehicleId = '';

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private vehicles: VehicleService,
    private groupAssign: GroupAssignmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.groupId || !this.groupType) return;
    this.loadAll();
  }

  private getVehicleGroupId(v: VehicleInfo): string | null {
    const g = this.groupType === 'MECHANIC' ? v.mechanicGroup : v.dispatcherGroup;
    return g?.id ?? null;
  }

  private isVehicleUnassignedForType(v: VehicleInfo): boolean {
    const g = this.groupType === 'MECHANIC' ? v.mechanicGroup : v.dispatcherGroup;
    return !g?.id;
  }

  loadAll(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    forkJoin({
      vehicles: this.vehicles.getAll().pipe(catchError(() => of([] as VehicleInfo[])))
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(({ vehicles }) => {
      this.allVehicles = vehicles ?? [];

      this.vehiclesInGroup = this.allVehicles.filter(v => this.getVehicleGroupId(v) === this.groupId);

      // "available": тези без група за този тип (mechanicGroup null / dispatcherGroup null)
      // + позволяваме и тези вече в нашата група (за да не изчезват от dropdown-a)
      const available = this.allVehicles.filter(v =>
        this.isVehicleUnassignedForType(v) || this.getVehicleGroupId(v) === this.groupId
      );

      // махаме тези, които вече са в групата (за да не ги assign-ваш пак)
      this.availableVehicles = available.filter(v => this.getVehicleGroupId(v) !== this.groupId);

      this.selectedVehicleId = '';
      this.loading = false;
      this.cdr.markForCheck();
    });
  }

  assign(): void {
    if (!this.selectedVehicleId || this.saving) return;

    this.saving = true;
    this.error = '';
    this.success = '';

    this.groupAssign.assignVehicle(this.groupType, this.groupId, this.selectedVehicleId).subscribe({
      next: () => {
        this.success = 'Успешно назначено ППС към групата.';
        this.selectedVehicleId = '';
        this.saving = false;
        this.loadAll();
      },
      error: (err) => {
        this.saving = false;
        if (err?.status === 401 || err?.status === 403) this.error = 'Нямаш достъп (authentication/permissions).';
        else this.error = err?.error?.message || 'Грешка при назначаване към група.';
        this.cdr.markForCheck();
      }
    });
  }

  remove(vehicleId: string): void {
    if (this.saving) return;

    this.saving = true;
    this.error = '';
    this.success = '';

    this.groupAssign.removeVehicle(this.groupType, this.groupId, vehicleId).subscribe({
      next: () => {
        this.success = 'ППС е махнато от групата.';
        this.saving = false;
        this.loadAll();
      },
      error: (err) => {
        this.saving = false;
        if (err?.status === 401 || err?.status === 403) this.error = 'Нямаш достъп (authentication/permissions).';
        else this.error = err?.error?.message || 'Грешка при махане от група.';
        this.cdr.markForCheck();
      }
    });
  }
}

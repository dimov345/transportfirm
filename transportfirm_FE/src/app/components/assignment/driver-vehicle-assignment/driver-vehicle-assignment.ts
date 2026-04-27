import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AssignmentService } from '../../../core/services/assignment.service';
import { VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-driver-vehicle-assignment',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './driver-vehicle-assignment.html',
  styleUrls: ['./driver-vehicle-assignment.scss']
})
export class DriverVehicleAssignment implements OnInit {
  @Input() driverInfoId!: string;

  loading = true;
  saving = false;

  error = '';
  success = '';

  currentVehicle: VehicleInfo | null = null;
  availableVehicles: VehicleInfo[] = [];
  selectedVehicleId = '';

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private assignmentService: AssignmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.driverInfoId) return;
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    forkJoin({
      current: this.assignmentService.getVehicleOfDriver(this.driverInfoId).pipe(
        catchError(() => of(null))
      ),
      available: this.assignmentService.getAvailableVehicles().pipe(
        catchError(() => of([]))
      )
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(({ current, available }) => {
      this.currentVehicle = (current as VehicleInfo | null) ?? null;
      this.availableVehicles = (available as VehicleInfo[]) ?? [];

      // ако има кола, няма смисъл да държим избрано в dropdown
      if (this.currentVehicle) {
        this.selectedVehicleId = '';
      }

      this.loading = false;

      // ✅ важно при OnPush / понякога при async updates
      this.cdr.markForCheck();
    });
  }

  assign(): void {
    if (!this.selectedVehicleId || this.saving) return;

    this.saving = true;
    this.error = '';
    this.success = '';

    this.assignmentService
      .assignVehicleToDriver(this.driverInfoId, this.selectedVehicleId)
      .subscribe({
        next: () => {
          this.success = 'Успешно назначено МПС.';
          this.selectedVehicleId = '';
          this.saving = false;
          this.loadAll();
        },
        error: (err) => {
          this.saving = false;

          if (err?.status === 409) {
            this.error =
              err?.error?.message ||
              'Конфликт: МПС е заето или шофьорът вече има МПС.';
          } else if (err?.status === 404) {
            this.error = err?.error?.message || 'Не е намерен шофьор или МПС.';
          } else if (err?.status === 401 || err?.status === 403) {
            this.error = 'Нямаш достъп (authentication/permissions).';
          } else {
            this.error = 'Грешка при назначаване.';
          }

          this.cdr.markForCheck();
        }
      });
  }

  unassign(): void {
    if (this.saving) return;

    this.saving = true;
    this.error = '';
    this.success = '';

    this.assignmentService
      .unassignVehicleFromDriver(this.driverInfoId)
      .subscribe({
        next: () => {
          this.success = 'МПС е откачено.';
          this.saving = false;
          this.loadAll();
        },
        error: (err) => {
          this.saving = false;

          if (err?.status === 401 || err?.status === 403) {
            this.error = 'Нямаш достъп (authentication/permissions).';
          } else {
            this.error = 'Грешка при откачане.';
          }

          this.cdr.markForCheck();
        }
      });
  }
}

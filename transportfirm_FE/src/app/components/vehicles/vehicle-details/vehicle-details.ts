import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { AssignmentService } from '../../../core/services/assignment.service';
import { DriverInfo } from '../../../core/models/driver/driver-info.model';

@Component({
  selector: 'app-vehicle-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vehicle-details.html',
  styleUrls: ['./vehicle-details.scss']
})
export class VehicleDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private vehicleService = inject(VehicleService);
  private assignmentService = inject(AssignmentService);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  vehicle: VehicleInfo | null = null;

  // ✅ DriverInfo за това ППС (има employee.name)
  assignedDriverInfo: DriverInfo | null = null;

  loading = false;
  errorMessage = '';

  private id = ''; // vehicle UUID

  ngOnInit() {
    if (!this.isBrowser) return;

    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (!this.id) {
      this.errorMessage = 'Не е предоставен ID на ППС.';
      this.router.navigate(['/vehicles']);
      return;
    }

    this.loadVehicle(this.id);
  }

  private loadVehicle(id: string) {
    this.loading = true;
    this.errorMessage = '';
    this.vehicle = null;
    this.assignedDriverInfo = null;
    this.cdr.detectChanges();

    this.vehicleService.getById(id).subscribe({
      next: (vehicle) => {
        this.vehicle = vehicle;
        this.loading = false;
        this.cdr.detectChanges();

        // ✅ след като имаме vehicle -> дърпаме шофьора
        this.loadAssignedDriver(vehicle.id);
      },
      error: (error) => {
        this.loading = false;

        if (error?.status === 401) {
          this.errorMessage = 'Нямаш активна сесия. Влез отново.';
        } else if (error?.status === 404) {
          this.errorMessage = 'ППС не е намерено.';
          this.router.navigate(['/vehicles']);
        } else {
          this.errorMessage = 'Грешка при зареждане на данните за превозното средство!';
        }

        console.error('VehicleDetails load error:', error);
        this.cdr.detectChanges();
      }
    });
  }

  private loadAssignedDriver(vehicleId: string) {
    this.assignedDriverInfo = null;
    this.cdr.detectChanges();

    this.assignmentService.getDriverOfVehicle(vehicleId).subscribe({
      next: (driverInfo) => {
        this.assignedDriverInfo = driverInfo;
        this.cdr.detectChanges();
      },
      error: () => {
        // ако няма назначен -> 404/204
        this.assignedDriverInfo = null;
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'Не е посочена';
    return new Date(dateString).toLocaleDateString('bg-BG');
  }

  isExpired(dateString?: string): boolean {
    if (!dateString) return false;
    return new Date(dateString) < new Date();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'В движение': return 'status-active';
      case 'С временно отстранени неизправности': return 'status-inactive';
      case 'Спряно от движение':
      case 'Повредено':
      case 'Бракувано': return 'status-stopped';
      default: return 'status-inactive';
    }
  }
}

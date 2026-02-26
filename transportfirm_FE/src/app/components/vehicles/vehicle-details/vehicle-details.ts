import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { AssignmentService } from '../../../core/services/assignment.service';
import { DriverInfo } from '../../../core/models/driver/driver-info.model';
import { AuthService } from '../../../core/auth/auth.service';
import { MaintenanceService, MaintenanceRecord } from '../../../core/services/maintenance.service';

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
  private auth = inject(AuthService);
  private maintenanceService = inject(MaintenanceService);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  vehicle: VehicleInfo | null = null;
  assignedDriverInfo: DriverInfo | null = null;
  maintenanceRecords: MaintenanceRecord[] = [];
  loadingMaintenance = false;

  loading = false;
  errorMessage = '';

  private id = '';

  get isAdminOrManager(): boolean {
    return this.auth.hasRole('ADMIN', 'MANAGER');
  }

  readonly maintenanceTypeLabels: Record<string, string> = {
    REPAIR: 'Ремонт',
    SERVICE: 'Сервиз',
    INSPECTION: 'Технически преглед',
    TIRE_CHANGE: 'Смяна на гуми',
    BREAKDOWN: 'Авария',
    ACCIDENT: 'Катастрофа',
    OTHER: 'Друго'
  };

  readonly maintenanceStatusClasses: Record<string, string> = {
    OPEN: 'mstatus-open',
    CLOSED: 'mstatus-closed',
    CANCELED: 'mstatus-canceled'
  };

  readonly maintenanceStatusLabels: Record<string, string> = {
    OPEN: 'Отворен',
    CLOSED: 'Затворен',
    CANCELED: 'Отменен'
  };

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

        this.loadAssignedDriver(vehicle.id);
        this.loadMaintenance(vehicle.id);
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

  private loadMaintenance(vehicleId: string) {
    this.loadingMaintenance = true;
    this.maintenanceRecords = [];
    this.cdr.detectChanges();

    this.maintenanceService.getByVehicle(vehicleId, 0, 50).subscribe({
      next: (page) => {
        this.maintenanceRecords = page.content;
        this.loadingMaintenance = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingMaintenance = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'Не е посочена';
    return new Date(dateString).toLocaleDateString('bg-BG');
  }

  formatDateTime(dateString?: string): string {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString('bg-BG');
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

  vehicleStatuses = [
    { value: 'AVAILABLE', label: 'Свободно' },
    { value: 'ON_ROUTE', label: 'На курс' },
    { value: 'IN_SERVICE', label: 'В сервиз' },
    { value: 'BROKEN_DOWN', label: 'Повредено' },
    { value: 'WAITING_PARTS', label: 'Чака части' },
    { value: 'OUT_OF_SERVICE', label: 'Извън експлоатация' }
  ];

  getVehicleStatusLabel(value?: string | null): string {
    if (!value) return '—';
    return this.vehicleStatuses.find(s => s.value === value)?.label ?? value;
  }

  getVehicleStatusClass(value?: string | null): string {
    switch (value) {
      case 'AVAILABLE': return 'status-available';
      case 'ON_ROUTE': return 'status-on-route';
      case 'IN_SERVICE': return 'status-in-service';
      case 'BROKEN_DOWN': return 'status-broken';
      case 'WAITING_PARTS': return 'status-waiting';
      case 'OUT_OF_SERVICE': return 'status-out';
      default: return '';
    }
  }

  
}

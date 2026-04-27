import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './vehicle-list.html',
  styleUrls: ['./vehicle-list.scss']
})
export class VehicleListComponent implements OnInit {
  vehicles: VehicleInfo[] = [];
  filteredVehicles: VehicleInfo[] = [];

  // Filters
  searchTerm = '';
  statusFilter = '';
  typeFilter = '';

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  get isDispatcher(): boolean {
    return this.auth.hasRole('DISPATCHER');
  }

  get isMechanic(): boolean {
    return this.auth.hasRole('MECHANIC');
  }

  constructor(private vehicleService: VehicleService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    if (!this.isBrowser) return;

    this.loadVehicles();
  }

  loadVehicles() {
    this.vehicleService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (vehicles) => {
        this.vehicles = vehicles;
        this.filteredVehicles = vehicles;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading vehicles:', error);
      }
    });
  }

  applyFilters() {
    const text = this.searchTerm.toLowerCase();

    this.filteredVehicles = this.vehicles.filter(v => {
      const matchesSearch =
        text === '' ||
        (v.plateNumber || '').toLowerCase().includes(text) ||
        (v.model || '').toLowerCase().includes(text) ||
        (v.owner || '').toLowerCase().includes(text);

      const matchesStatus =
        this.statusFilter === '' || v.technicalCondition === this.statusFilter;

      const matchesType =
        this.typeFilter === '' || v.typePps === this.typeFilter;

      return matchesSearch && matchesStatus && matchesType;
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'В движение': return 'status-active';
      case 'Спряно от движение':
      case 'Повредено':
      case 'Бракувано': return 'status-stopped';
      default: return 'status-inactive';
    }
  }

  isExpired(dateString?: string): boolean {
    if (!dateString) return false;
    return new Date(dateString).getTime() < new Date().getTime();
  }

  isExpiring(dateString?: string): boolean {
    if (!dateString) return false;

    const expiryDate = new Date(dateString);
    const today = new Date();
    const oneMonthFromNow = new Date();
    oneMonthFromNow.setMonth(today.getMonth() + 1);

    return expiryDate.getTime() > today.getTime() && expiryDate.getTime() <= oneMonthFromNow.getTime();
  }

  deleteVehicle(id: string) {
    if (!confirm('Наистина ли искате да изтриете това превозно средство?')) return;

    this.vehicleService.delete(id).subscribe({
      next: () => {
        this.vehicles = this.vehicles.filter(v => v.id !== id);
        this.applyFilters();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error deleting vehicle:', error);
        alert(error?.error?.message || 'Грешка при изтриване на превозното средство!');
      }
    });
  }

  isAnyExpired(vehicle: VehicleInfo): boolean {
    return (
      this.isExpired(vehicle.kaskoDo) ||
      this.isExpired(vehicle.grazhdanskaOtgovornostDo) ||
      this.isExpired(vehicle.gtpDo) ||
      this.isExpired(vehicle.vinetkaDo)
    );
  }

  isAnyExpiring(vehicle: VehicleInfo): boolean {
    return (
      this.isExpiring(vehicle.kaskoDo) ||
      this.isExpiring(vehicle.grazhdanskaOtgovornostDo) ||
      this.isExpiring(vehicle.gtpDo) ||
      this.isExpiring(vehicle.vinetkaDo)
    );
  }

  trackById(index: number, v: VehicleInfo) {
    return v.id;
  }

  vehicleStatuses = [
  { value: 'AVAILABLE', label: 'Свободно' },
  { value: 'ON_ROUTE', label: 'На курс' },
  { value: 'IN_SERVICE', label: 'В сервиз' },
  { value: 'BROKEN_DOWN', label: 'Повредено' },
  { value: 'WAITING_PARTS', label: 'Чака части' },
  { value: 'OUT_OF_SERVICE', label: 'Извън експлоатация' }
];

  vehicleStatusFilter: string = ''; // нов филтър (value от enum)

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

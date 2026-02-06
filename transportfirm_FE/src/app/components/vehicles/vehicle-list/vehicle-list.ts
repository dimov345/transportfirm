import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
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

  constructor(private vehicleService: VehicleService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    if (!this.isBrowser) return;

    // debug (browser-only)
    console.log('LS auth_token:', localStorage.getItem('auth_token'));
    console.log('Cookie:', document.cookie);

    this.loadVehicles();
  }

  loadVehicles() {
    this.vehicleService.getAll().subscribe({
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
}

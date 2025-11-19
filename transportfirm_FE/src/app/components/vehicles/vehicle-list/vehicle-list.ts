import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle';

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

  constructor(private vehicleService: VehicleService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadVehicles();
  }

  // Load vehicles without unnecessary loading indicator
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

  // Apply all filters
  applyFilters() {
    const text = this.searchTerm.toLowerCase();

    this.filteredVehicles = this.vehicles.filter(v => {
      const matchesSearch =
        text === '' ||
        v.plateNumber.toLowerCase().includes(text) ||
        v.model.toLowerCase().includes(text) ||
        v.owner?.toLowerCase().includes(text);

      const matchesStatus =
        this.statusFilter === '' || v.technicalCondition === this.statusFilter;

      const matchesType =
        this.typeFilter === '' || v.typePps === this.typeFilter;

      return matchesSearch && matchesStatus && matchesType;
    });
  }

  // Status badge styling
  getStatusClass(status: string): string {
    switch (status) {
      case 'В движение': return 'status-active';
      case 'Спряно от движение':
      case 'Повредено':
      case 'Бракувано': return 'status-stopped';
      default: return 'status-inactive';
    }
  }

  // Check if a document date is expired
  isExpired(dateString?: string): boolean {
    if (!dateString) return false;
    return new Date(dateString).getTime() < new Date().getTime();
  }

  // Check if a document expires within the next month
  isExpiring(dateString?: string): boolean {
    if (!dateString) return false;
    
    const expiryDate = new Date(dateString);
    const today = new Date();
    const oneMonthFromNow = new Date();
    oneMonthFromNow.setMonth(today.getMonth() + 1);
    
    return expiryDate.getTime() > today.getTime() && expiryDate.getTime() <= oneMonthFromNow.getTime();
  }

  // Check if any vehicle document is expired
  hasExpiredDocuments(vehicle: VehicleInfo): boolean {
    return (
      this.isExpired(vehicle.kaskoDo) ||
      this.isExpired(vehicle.grazhdanskaOtgovornostDo) ||
      this.isExpired(vehicle.gtpDo)
    );
  }

  // Check if any vehicle document is expiring soon
  hasExpiringDocuments(vehicle: VehicleInfo): boolean {
    return (
      this.isExpiring(vehicle.kaskoDo) ||
      this.isExpiring(vehicle.grazhdanskaOtgovornostDo) ||
      this.isExpiring(vehicle.gtpDo)
    );
  }

  // Get document status class for styling
  getDocumentStatusClass(dateString?: string): string {
    if (!dateString) return '';
    
    if (this.isExpired(dateString)) return 'expired';
    if (this.isExpiring(dateString)) return 'expiring';
    
    return '';
  }

  // Format date for UI
  formatDate(dateString?: string): string {
    if (!dateString) return 'Няма';
    return new Date(dateString).toLocaleDateString('bg-BG', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  // Delete a vehicle
  deleteVehicle(plateNumber: string) {
    if (!confirm('Наистина ли искате да изтриете това превозно средство?')) return;

    this.vehicleService.delete(plateNumber).subscribe({
      next: () => {
        this.vehicles = this.vehicles.filter(v => v.plateNumber !== plateNumber);
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error deleting vehicle:', error);
      }
    });
  }
}
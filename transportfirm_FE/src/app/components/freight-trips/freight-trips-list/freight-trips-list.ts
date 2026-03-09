import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { FreightTripService } from '../../../core/services/freight-trip.service';
import { FreightTrip, TripStatus } from '../../../core/models/freight-trip.model';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-freight-trips-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './freight-trips-list.html',
  styleUrls: ['./freight-trips-list.scss']
})
export class FreightTripsListComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private tripService = inject(FreightTripService);
  private vehicleService = inject(VehicleService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  auth = inject(AuthService);

  trips: FreightTrip[] = [];
  vehicles: VehicleInfo[] = [];

  loading = true;
  error = '';

  filters = {
    search: '',
    status: '',
    vehicleId: ''
  };

  page = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;

  readonly statusLabels: Record<string, string> = {
    PLANNED: 'Планиран',
    IN_TRANSIT: 'В движение',
    COMPLETED: 'Завършен'
  };

  readonly statusClasses: Record<string, string> = {
    PLANNED: 'status-inactive',
    IN_TRANSIT: 'status-leave',
    COMPLETED: 'status-active'
  };

  ngOnInit(): void {
    if (!this.isBrowser) return;
    this.loadVehicles();
    this.loadTrips();
  }

  private loadVehicles(): void {
    this.vehicleService.getAll().subscribe({
      next: (vehicles) => {
        this.vehicles = vehicles;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  loadTrips(): void {
    this.loading = true;
    this.tripService.getPaged(
      this.filters.search,
      this.filters.status,
      this.filters.vehicleId,
      this.page,
      this.pageSize
    ).subscribe({
      next: (page) => {
        this.trips = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Грешка при зареждане на курсовете.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadTrips();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.loadTrips();
  }

  openTrip(id?: string): void {
    if (id) this.router.navigate(['/freight-trips', id]);
  }

  newTrip(): void {
    this.router.navigate(['/freight-trips/new']);
  }

  netProfit(trip: FreightTrip): number {
    const revenue = trip.revenueEur ?? 0;
    const vat = trip.vatEur ?? 0;
    const tolls = trip.tollFeesEur ?? 0;
    const fuel = trip.fuelCostEur ?? 0;
    const border = trip.borderFeesEur ?? 0;
    const parking = trip.parkingAccommodationEur ?? 0;
    const other = trip.otherExpensesEur ?? 0;
    return revenue - vat - tolls - fuel - border - parking - other;
  }

  formatDate(d?: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('bg-BG');
  }

  formatEur(val?: number | null): string {
    if (val == null) return '—';
    return val.toFixed(2) + ' €';
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  get isAdminOrManager(): boolean {
    return this.auth.hasRole('ADMIN', 'MANAGER');
  }

  trackById = (_: number, t: FreightTrip) => t.id;
}

import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';

import { FreightTripService } from '../../../core/services/freight-trip.service';
import { FreightTrip } from '../../../core/models/freight-trip.model';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-freight-trip-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './freight-trip-details.html',
  styleUrls: ['./freight-trip-details.scss']
})
export class FreightTripDetailsComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private tripService = inject(FreightTripService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  auth = inject(AuthService);

  trip: FreightTrip | null = null;
  loading = true;
  error = '';
  deleting = false;

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
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/freight-trips']);
      return;
    }
    this.tripService.getById(id).pipe(
      catchError(() => {
        this.error = 'Курсът не е намерен.';
        this.loading = false;
        this.cdr.detectChanges();
        return of(null);
      })
    ).subscribe(trip => {
      if (!trip) return;
      this.trip = trip;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  edit(): void {
    if (this.trip?.id) this.router.navigate(['/freight-trips/edit', this.trip.id]);
  }

  goBack(): void {
    this.router.navigate(['/freight-trips']);
  }

  delete(): void {
    if (!this.trip?.id) return;
    if (!confirm('Наистина ли искате да изтриете този курс?')) return;
    this.deleting = true;
    this.tripService.delete(this.trip.id).subscribe({
      next: () => this.router.navigate(['/freight-trips']),
      error: () => {
        this.deleting = false;
        alert('Грешка при изтриване!');
        this.cdr.detectChanges();
      }
    });
  }

  netProfit(): number {
    if (!this.trip) return 0;
    const t = this.trip;
    return (t.revenueEur ?? 0)
      - (t.vatEur ?? 0)
      - (t.tollFeesEur ?? 0)
      - (t.fuelCostEur ?? 0)
      - (t.borderFeesEur ?? 0)
      - (t.parkingAccommodationEur ?? 0)
      - (t.otherExpensesEur ?? 0);
  }

  totalExpenses(): number {
    if (!this.trip) return 0;
    const t = this.trip;
    return (t.vatEur ?? 0)
      + (t.tollFeesEur ?? 0)
      + (t.fuelCostEur ?? 0)
      + (t.borderFeesEur ?? 0)
      + (t.parkingAccommodationEur ?? 0)
      + (t.otherExpensesEur ?? 0);
  }

  formatDate(d?: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('bg-BG');
  }

  formatEur(val?: number | null): string {
    if (val == null) return '—';
    return val.toFixed(2) + ' €';
  }

  get isAdminOrManager(): boolean {
    return this.auth.hasRole('ADMIN', 'MANAGER');
  }
}

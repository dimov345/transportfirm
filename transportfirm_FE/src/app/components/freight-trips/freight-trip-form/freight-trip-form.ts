import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { catchError, of } from 'rxjs';

import { FreightTripService } from '../../../core/services/freight-trip.service';
import { invalidateCache } from '../../../core/interceptors/cache.interceptor';
import { FreightTrip, TripStatus } from '../../../core/models/freight-trip.model';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-freight-trip-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './freight-trip-form.html',
  styleUrls: ['./freight-trip-form.scss']
})
export class FreightTripFormComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  private fb = inject(FormBuilder);
  private tripService = inject(FreightTripService);
  private vehicleService = inject(VehicleService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  form!: FormGroup;
  vehicles: VehicleInfo[] = [];

  isEdit = false;
  tripId: string | null = null;

  /** When navigated from dispatcher dashboard — vehicle is pre-set and locked */
  lockedVehicleId: string | null = null;
  lockedVehicle: VehicleInfo | null = null;

  /** True when the locked vehicle already has an active IN_TRANSIT trip */
  vehicleInTransit = false;

  loading = true;
  saving = false;

  ngOnInit(): void {
    if (!this.isBrowser) return;

    this.form = this.fb.group({
      vehicleId: ['', Validators.required],
      status: ['PLANNED', Validators.required],
      departureDate: ['', Validators.required],
      arrivalDate: [''],
      originCity: ['', Validators.required],
      destinationCity: ['', Validators.required],
      distanceKm: [null],
      clientName: [''],
      clientEik: [''],
      clientAddress: [''],
      sellerName: [''],
      sellerEik: [''],
      sellerAddress: [''],
      revenueEur: [null],
      vatEur: [null],
      tollFeesEur: [null],
      fuelLiters: [null],
      fuelCostEur: [null],
      borderFeesEur: [null],
      parkingAccommodationEur: [null],
      otherExpensesEur: [null],
      cargoDescription: [''],
      cargoWeightTons: [null],
      notes: ['']
    });

    // Auto-calculate VAT at 20% when revenue changes
    this.form.get('revenueEur')!.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((revenue: number | null) => {
      if (revenue != null && revenue > 0) {
        const vat = Math.round(revenue * 0.20 * 100) / 100;
        this.form.patchValue({ vatEur: vat }, { emitEvent: false });
      }
    });

    // Check for locked vehicleId from query params (dispatcher dashboard flow)
    const qVehicleId = this.route.snapshot.queryParamMap.get('vehicleId');
    if (qVehicleId) {
      this.lockedVehicleId = qVehicleId;
      this.form.patchValue({ vehicleId: qVehicleId });
      this.form.get('vehicleId')!.disable();

      // Check if this vehicle already has an active IN_TRANSIT trip
      this.tripService.getByVehicleId(qVehicleId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (trips) => {
          this.vehicleInTransit = trips.some((t: FreightTrip) => t.status === 'IN_TRANSIT');
          if (this.vehicleInTransit && this.form.get('status')!.value === 'IN_TRANSIT') {
            this.form.patchValue({ status: 'PLANNED' });
          }
          this.cdr.detectChanges();
        },
        error: () => {}
      });
    }

    // Load all vehicles (for edit mode or when no locked vehicle)
    this.vehicleService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (v) => {
        this.vehicles = v;
        if (this.lockedVehicleId) {
          this.lockedVehicle = v.find(veh => veh.id === this.lockedVehicleId) ?? null;
        }
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    // Edit mode: load existing trip
    const editId = this.route.snapshot.paramMap.get('id');
    if (editId) {
      this.isEdit = true;
      this.tripId = editId;
      this.tripService.getById(editId).pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => {
          alert('Курсът не е намерен.');
          this.router.navigate(['/freight-trips']);
          return of(null);
        })
      ).subscribe(trip => {
        if (!trip) return;
        this.form.patchValue({
          vehicleId: trip.vehicle?.id ?? '',
          status: trip.status,
          departureDate: trip.departureDate ?? '',
          arrivalDate: trip.arrivalDate ?? '',
          originCity: trip.originCity,
          destinationCity: trip.destinationCity,
          distanceKm: trip.distanceKm ?? null,
          clientName: trip.clientName ?? '',
          clientEik: trip.clientEik ?? '',
          clientAddress: trip.clientAddress ?? '',
          sellerName: trip.sellerName ?? '',
          sellerEik: trip.sellerEik ?? '',
          sellerAddress: trip.sellerAddress ?? '',
          revenueEur: trip.revenueEur ?? null,
          vatEur: trip.vatEur ?? null,
          tollFeesEur: trip.tollFeesEur ?? null,
          fuelLiters: trip.fuelLiters ?? null,
          fuelCostEur: trip.fuelCostEur ?? null,
          borderFeesEur: trip.borderFeesEur ?? null,
          parkingAccommodationEur: trip.parkingAccommodationEur ?? null,
          otherExpensesEur: trip.otherExpensesEur ?? null,
          cargoDescription: trip.cargoDescription ?? '',
          cargoWeightTons: trip.cargoWeightTons ?? null,
          notes: trip.notes ?? ''
        });
        this.loading = false;
        this.cdr.detectChanges();
      });
    } else {
      this.loading = false;
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const raw = this.form.getRawValue(); // getRawValue includes disabled controls
    const vehicleId: string = raw.vehicleId;

    const trip: FreightTrip = {
      status: raw.status,
      departureDate: raw.departureDate,
      arrivalDate: raw.arrivalDate || undefined,
      originCity: raw.originCity,
      destinationCity: raw.destinationCity,
      distanceKm: raw.distanceKm ?? undefined,
      clientName: raw.clientName || undefined,
      clientEik: raw.clientEik || undefined,
      clientAddress: raw.clientAddress || undefined,
      sellerName: raw.sellerName || undefined,
      sellerEik: raw.sellerEik || undefined,
      sellerAddress: raw.sellerAddress || undefined,
      revenueEur: raw.revenueEur ?? undefined,
      vatEur: raw.vatEur ?? undefined,
      tollFeesEur: raw.tollFeesEur ?? undefined,
      fuelLiters: raw.fuelLiters ?? undefined,
      fuelCostEur: raw.fuelCostEur ?? undefined,
      borderFeesEur: raw.borderFeesEur ?? undefined,
      parkingAccommodationEur: raw.parkingAccommodationEur ?? undefined,
      otherExpensesEur: raw.otherExpensesEur ?? undefined,
      cargoDescription: raw.cargoDescription || undefined,
      cargoWeightTons: raw.cargoWeightTons ?? undefined,
      notes: raw.notes || undefined
    };

    if (this.isEdit && this.tripId) {
      this.tripService.update(this.tripId, trip, vehicleId).subscribe({
        next: (saved) => {
          this.saving = false;
          invalidateCache();
          this.router.navigate(['/freight-trips', saved.id]);
        },
        error: (err) => {
          this.saving = false;
          alert(err.status === 409
            ? (err.error?.message ?? 'Това ППС вече е в активен курс. Може да зададете само Планиран статус.')
            : 'Грешка при запис!');
          this.cdr.detectChanges();
        }
      });
    } else {
      this.tripService.create(trip, vehicleId).subscribe({
        next: (saved) => {
          this.saving = false;
          invalidateCache();
          this.router.navigate(['/freight-trips', saved.id]);
        },
        error: (err) => {
          this.saving = false;
          alert(err.status === 409
            ? (err.error?.message ?? 'Това ППС вече е в активен курс. Може да добавите само Планиран курс.')
            : 'Грешка при запис!');
          this.cdr.detectChanges();
        }
      });
    }
  }

  cancel(): void {
    if (this.lockedVehicleId) {
      this.router.navigate(['/dispatcher']);
    } else if (this.isEdit && this.tripId) {
      this.router.navigate(['/freight-trips', this.tripId]);
    } else {
      this.router.navigate(['/freight-trips']);
    }
  }

  hasError(field: string): boolean {
    const c = this.form.get(field);
    return !!(c && c.invalid && c.touched);
  }
}

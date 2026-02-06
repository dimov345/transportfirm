import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-vehicle-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vehicle-details.html',
  styleUrls: ['./vehicle-details.scss']
})
export class VehicleDetails implements OnInit {
  vehicle!: VehicleInfo;

  private id = ''; // UUID

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private vehicleService: VehicleService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (!this.id) {
      console.error('Не е предоставен id на ППС');
      this.router.navigate(['/vehicles']);
      return;
    }

    this.loadVehicle(this.id);
  }

  loadVehicle(id: string) {
    this.vehicleService.getById(id).subscribe({
      next: (vehicle) => {
        this.vehicle = vehicle;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Грешка при зареждане на данните за превозното средство!', error);
        if (error.status === 404) {
          console.error('Превозното средство не е намерено');
          this.router.navigate(['/vehicles']);
        }
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

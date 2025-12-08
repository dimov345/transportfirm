import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService, DriverInfo, DriverListItem } from '../../../core/services/driver';

@Component({
  standalone: true,
  selector: 'app-drivers-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './drivers-list.html',
  styleUrls: ['./drivers-list.scss']
})
export class DriversListComponent implements OnInit {

  drivers: DriverListItem[] = [];
  allDrivers: DriverListItem[] = [];
  searchText = "";
  loading = true;

  constructor(
    private driverService: DriverService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadDrivers();
  }

  loadDrivers() {
    this.driverService.getAllDriversFromEmployee().subscribe({
      next: (drivers) => {
        this.drivers = drivers;
        this.allDrivers = drivers;
        this.markStatuses();
        this.loading = false;        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading drivers:', error);
        this.loading = false;        
      }
    });
  }



  isExpired(d: DriverListItem): boolean {
    const fields = [
      d.driverLicenseExpiresOn,
      d.qualificationCardExpiresOn,
      d.psychologicalExamExpiresOn,
      d.digitalCardExpiresOn
    ];
    return fields.some(date => !!date && new Date(date) < new Date());
  }

  isExpiring(d: DriverListItem): boolean {
    const now = new Date();
    const in30 = new Date(now.getTime() + (30 * 24 * 60 * 60 * 1000));

    const fields = [
      d.driverLicenseExpiresOn,
      d.qualificationCardExpiresOn,
      d.psychologicalExamExpiresOn,
      d.digitalCardExpiresOn
    ];

    return fields.some(date =>
      !!date && new Date(date) > now && new Date(date) <= in30
    );
  }

  markStatuses() {
  this.allDrivers = this.allDrivers.map(d => {
    const expired = this.isExpired(d);
    const expiring = this.isExpiring(d);

    if (expired) {
      d.statusClass = 'expired-row';
      d.statusTooltip = 'Документът е изтекъл';
    } 
    else if (expiring) {
      d.statusClass = 'expiring-row';
      d.statusTooltip = 'Остава по-малко от месец';
    } 
    else {
      d.statusClass = '';
      d.statusTooltip = '';
    }

    return d;
  });

  this.drivers = [...this.allDrivers];
}


  // === Search ===
  applyFilter() {
    const t = this.searchText.toLowerCase().trim();

    if (!t) {
      this.drivers = [...this.allDrivers];
      return;
    }

    this.drivers = this.allDrivers.filter(d =>
      d.name?.toLowerCase().includes(t) ||
      d.egn?.includes(t)
    );
  }

  trackByEgn(index: number, d: DriverListItem) {
    return d.egn;
  }

  // === ROUTES ===
  driverDetail(id: number) {
    this.router.navigate(['/drivers', id]);
  }

  viewDocuments(id: number) {
    this.router.navigate(['/driver-documents', id]);
  }

  addDriverInfo(id: number) {
    this.router.navigate(['/drivers/edit', id]);
  }

  deleteDriver(id: number) {
    if (!confirm('Да изтрием ли шофьора?')) return;
    this.driverService.delete(id).subscribe(() => {
      this.allDrivers = this.allDrivers.filter(d => d.id !== id);
      this.applyFilter();
      this.cdr.detectChanges();
    });
  }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService, DriverInfo } from '../../../core/services/driver';

@Component({
  selector: 'app-drivers-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './drivers-list.html',
  styleUrls: ['./drivers-list.scss']
})
export class DriversListComponent implements OnInit {
  
  drivers: any[] = [];
  allDrivers: any[] = []; // ✔ за филтъра

  searchText = ""; // ✔ input модел

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
    this.loading = true;

    this.driverService.getAll().subscribe(drivers => {

      const mapped = drivers.map(d => ({
        ...d,
        statusClass: this.getStatusClass(d),
        statusTooltip: this.getStatusTooltip(d)
      }));

      this.drivers = mapped;
      this.allDrivers = [...mapped]; // ✔ за търсене

      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  // === SEARCH FILTER ===
  applyFilter() {
    const text = this.searchText.toLowerCase().trim();

    if (!text) {
      this.drivers = [...this.allDrivers];
      return;
    }

    this.drivers = this.allDrivers.filter(d =>
      d.name.toLowerCase().includes(text) ||
      d.egn.includes(text)
    );
  }

  // Останалата логика (status, delete, etc.) остава същата...
  
  trackByEgn(index: number, driver: DriverInfo) {
    return driver.egn;
  }

  hasExpiredDocuments(driver: DriverInfo): boolean {
    const today = new Date();
    return [
      driver.driverLicenseExpiresOn,
      driver.qualificationCardExpiresOn,
      driver.psychologicalExamExpiresOn,
      driver.digitalCardExpiresOn
    ].some(date => date && new Date(date) < today);
  }

  hasExpiringDocuments(driver: DriverInfo): boolean {
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);

    return [
      driver.driverLicenseExpiresOn,
      driver.qualificationCardExpiresOn,
      driver.psychologicalExamExpiresOn,
      driver.digitalCardExpiresOn
    ].some(date => {
      if (!date) return false;
      const exp = new Date(date);
      return exp > today && exp <= in30Days;
    });
  }

  getStatusClass(driver: DriverInfo): string {
    if (this.hasExpiredDocuments(driver)) return 'expired-row';
    if (this.hasExpiringDocuments(driver)) return 'expiring-row';
    return '';
  }

  getStatusTooltip(driver: DriverInfo): string {
    if (this.hasExpiredDocuments(driver)) return 'Има изтекли документи';
    if (this.hasExpiringDocuments(driver)) return 'Има документи, които изтичат скоро';
    return 'Всички документи са валидни';
  }

  deleteDriver(egn: string) {
    if (!confirm('Сигурни ли сте, че искате да изтриете шофьора?')) return;

    this.driverService.delete(egn).subscribe(() => {
      this.allDrivers = this.allDrivers.filter(d => d.egn !== egn);
      this.applyFilter(); // ✔ за да не чупи филтъра
      this.cdr.detectChanges();
    });
  }

  addDriver() {
    this.router.navigate(['/drivers/new']);
  }

  editDriver(egn: string) {
    this.router.navigate(['/drivers', egn]);
  }

  viewDocuments(egn: string) {
    this.router.navigate(['/driver-documents', egn]);
  }
}

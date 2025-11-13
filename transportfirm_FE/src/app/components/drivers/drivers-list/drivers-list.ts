import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DriverService, DriverInfo } from '../../../core/services/driver';

@Component({
  selector: 'app-drivers-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drivers-list.html',
  styleUrls: ['./drivers-list.scss']
})
export class DriversListComponent implements OnInit {
  drivers: DriverInfo[] = [];

  constructor(
    private driverService: DriverService, 
    private router: Router, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadDrivers();
  }

  loadDrivers() {
    this.driverService.getAll().subscribe(drivers => {
      this.drivers = drivers;
      this.cdr.detectChanges();
    });
  }

  // Проверка дали има изтекли документи
  hasExpiredDocuments(driver: DriverInfo): boolean {
    const today = new Date();
    
    return [
      driver.driverLicenseExpiresOn,
      driver.qualificationCardExpiresOn,
      driver.psychologicalExamExpiresOn,
      driver.digitalCardExpiresOn
    ].some(date => date && new Date(date) < today);
  }

  // Проверка дали има документи, които изтичат скоро (в рамките на 30 дни)
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
      const expiryDate = new Date(date);
      return expiryDate > today && expiryDate <= in30Days;
    });
  }

  // Връща CSS клас според статуса на документите
  getRowStatusClass(driver: DriverInfo): string {
    if (this.hasExpiredDocuments(driver)) {
      return 'expired-row';
    } else if (this.hasExpiringDocuments(driver)) {
      return 'expiring-row';
    }
    return '';
  }

  // Връща текст за tooltip със статуса
  getRowStatusTooltip(driver: DriverInfo): string {
    if (this.hasExpiredDocuments(driver)) {
      return 'Има изтекли документи';
    } else if (this.hasExpiringDocuments(driver)) {
      return 'Има документи, които изтичат скоро';
    }
    return 'Всички документи са валидни';
  }

  deleteDriver(egn: string) {
    if (confirm('Сигурни ли сте, че искате да изтриете шофьора?')) {
      this.driverService.delete(egn).subscribe(() => this.loadDrivers());
    }
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
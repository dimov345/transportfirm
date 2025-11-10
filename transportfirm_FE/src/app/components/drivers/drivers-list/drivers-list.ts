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

  constructor(private driverService: DriverService, private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadDrivers();
  }

   loadDrivers() {
    this.driverService.getAll().subscribe(drivers => {
      this.drivers = drivers;
      this.cdr.detectChanges(); // ✅ принудително update на view
    });
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

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService, DriverInfo, DriverDocument, Employee } from '../../../core/services/driver.service';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'app-drivers-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './drivers-list.html',
  styleUrls: ['./drivers-list.scss']
})
export class DriversListComponent implements OnInit {

  drivers: DriverInfo[] = [];
  allDrivers: DriverInfo[] = [];
  searchText = "";
  loading = true;

  filters = {
  search: '',
  status: '',       // expired | expiring | valid | missing
  expiringIn: '',   // 7 | 30 | 90
  email: ''         // yes | no
};


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
    next: employees => {

      const requests = employees.map(e =>
        this.driverService.getDriverFullInfo(e.id).pipe(
          // Ако няма DriverInfo → правим празен
          map(info => {
            return {
              id: e.id,
              employee: e,
              driverLicenseExpiresOn: info?.driverLicenseExpiresOn || null,
              qualificationCardExpiresOn: info?.qualificationCardExpiresOn || null,
              psychologicalExamExpiresOn: info?.psychologicalExamExpiresOn || null,
              digitalCardExpiresOn: info?.digitalCardExpiresOn || null,
              statusClass: '',
              statusTooltip: ''
            } as DriverInfo;
          })
        )
      );

      forkJoin(requests).subscribe({
        next: (driverInfos: DriverInfo[]) => {
          this.allDrivers = driverInfos;
          this.markStatuses();
          this.drivers = driverInfos;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: err => {
          console.error("Error loading driver info:", err);
          this.loading = false;
        }
      });

    },
    error: err => {
      console.error("Error loading employees:", err);
      this.loading = false;
    }
  });
}




  isExpired(d: DriverInfo): boolean {
    const fields = [
      d.driverLicenseExpiresOn,
      d.qualificationCardExpiresOn,
      d.psychologicalExamExpiresOn,
      d.digitalCardExpiresOn
    ];
    return fields.some(date => !!date && new Date(date) < new Date());
  }

  isExpiring(d: DriverInfo): boolean {
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
      d.statusTooltip = 'Документ е изтекъл';
    } 
    else if (expiring) {
      d.statusClass = 'expiring-row';
      d.statusTooltip = 'Изтичащ документ';
    } 
    else {
      d.statusClass = '';
      d.statusTooltip = '';
    }

    return d;
  });

  this.drivers = this.allDrivers;
}


  // === Search ===
 applyFilters() {
  let result = [...this.allDrivers];
  const now = new Date();

  // Search
  if (this.filters.search) {
    const t = this.filters.search.toLowerCase().trim();
    result = result.filter(d =>
      d.employee?.name.toLowerCase().includes(t) ||
      d.employee?.egn.includes(t)
    );
  }

  // Status
  if (this.filters.status) {
    result = result.filter(d => {
      const hasDocs = d.driverLicenseExpiresOn ||
                      d.qualificationCardExpiresOn ||
                      d.psychologicalExamExpiresOn ||
                      d.digitalCardExpiresOn;

      if (this.filters.status === 'missing') {
        return !hasDocs;
      }

      if (this.filters.status === 'expired') {
        return this.isExpired(d);
      }

      if (this.filters.status === 'expiring') {
        return this.isExpiring(d);
      }

      if (this.filters.status === 'valid') {
        return hasDocs && !this.isExpired(d) && !this.isExpiring(d);
      }

      return true;
    });
  }

  // Expiring in X days
  if (this.filters.expiringIn) {
    const days = +this.filters.expiringIn;
    const limit = new Date(now.getTime() + days * 86400000);

    result = result.filter(d =>
      [
        d.driverLicenseExpiresOn,
        d.qualificationCardExpiresOn,
        d.psychologicalExamExpiresOn,
        d.digitalCardExpiresOn
      ].some(date =>
        date && new Date(date) > now && new Date(date) <= limit
      )
    );
  }

  // Email
  if (this.filters.email) {
    result = result.filter(d =>
      this.filters.email === 'yes'
        ? !!d.employee?.email
        : !d.employee?.email
    );
  }

  this.drivers = result;
}

exportCsv() {
  this.loading = true;

  // Подаваме текущите филтри към backend
  const params: any = {};
  if (this.filters.search) params.search = this.filters.search;
  if (this.filters.status) params.status = this.filters.status.toUpperCase(); // backend очаква EXPIRED / EXPIRING / VALID

  this.driverService.exportDriversCsv(params).subscribe({
    next: res => {
      this.loading = false;
      const fileName = res.fileName;

      // Директно отваряме download в нов таб
      window.open(`http://localhost:8080/api/drivers/export/download/${fileName}`, '_blank');
    },
    error: err => {
      console.error('CSV export failed', err);
      this.loading = false;
      alert('Грешка при експортиране на CSV');
    }
  });
}




  trackByEgn(index: number, d: DriverInfo) {
    return d.employee?.egn;
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
      this.applyFilters();
      this.cdr.detectChanges();
    });
  }
}

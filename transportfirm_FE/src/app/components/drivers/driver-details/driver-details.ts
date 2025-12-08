import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-driver-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-details.html',
  styleUrls: ['./driver-details.scss']
})
export class DriverDetails implements OnInit {
  driver: any = null;
  error = '';
  noInfo = false; // 👉 НОВО - ако няма DriverInfo запис
  id!: string | null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id'); // 🔥 вече използваме id, НЕ egn
    this.loadDriverDetails();
  }

  loadDriverDetails() {
    if (!this.id) {
      this.error = 'Не е посочен ID на шофьора';
      return;
    }

    this.http.get(`http://localhost:8080/api/drivers/${this.id}`)
      .pipe(
        catchError(err => {
          this.error = 'Грешка при зареждане на данните за шофьора';
          return of(null);
        })
      )
      .subscribe(data => {

        if (!data) return;

        // 👉 Backend връща "NO_INFO"
        if (data === "NO_INFO") {
          this.noInfo = true;
          return;
        }

        this.driver = data;

        // 👉 гарантираме, че employee винаги съществува
        if (!this.driver.employee) {
          this.driver.employee = {
            name: '',
            egn: '',
            phone: '',
            email: ''
          };
        }

        this.cdr.detectChanges();
      });
  }

  // ---------------- DOCUMENT STATUS ------------------

  get hasExpiredDocuments(): boolean {
    if (!this.driver) return false;
    const today = new Date();
    
    return [
      this.driver.driverLicenseExpiresOn,
      this.driver.qualificationCardExpiresOn,
      this.driver.psychologicalExamExpiresOn,
      this.driver.digitalCardExpiresOn
    ].some(date => date && new Date(date) < today);
  }

  get expiredDocumentsCount(): number {
    if (!this.driver) return 0;
    const today = new Date();
    
    return [
      this.driver.driverLicenseExpiresOn,
      this.driver.qualificationCardExpiresOn,
      this.driver.psychologicalExamExpiresOn,
      this.driver.digitalCardExpiresOn
    ].filter(date => date && new Date(date) < today).length;
  }

  get expiringDocumentsCount(): number {
    if (!this.driver) return 0;
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
    
    return [
      this.driver.driverLicenseExpiresOn,
      this.driver.qualificationCardExpiresOn,
      this.driver.psychologicalExamExpiresOn,
      this.driver.digitalCardExpiresOn
    ].filter(date => {
      if (!date) return false;
      const expiry = new Date(date);
      return expiry > today && expiry <= in30Days;
    }).length;
  }

  isExpired(date: string | null): boolean {
    return !!date && new Date(date) < new Date();
  }

  isExpiring(date: string | null): boolean {
    if (!date) return false;
    const expiry = new Date(date);
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
    return expiry > today && expiry <= in30Days;
  }

  getDocumentStatus(date: string | null): string {
    if (!date) return 'Няма данни';
    if (this.isExpired(date)) return 'Изтекъл';
    if (this.isExpiring(date)) return 'Изтича скоро';
    return 'Валиден';
  }

  formatDate(date: string | null) {
    return date ? new Date(date).toLocaleDateString('bg-BG') : '—';
  }

  // ---------------- ACTIONS ------------------

  goBack() {
    this.router.navigate(['/drivers']);
  }

  editDriver() {
    if (this.driver && this.id) {
      this.router.navigate(['/drivers/edit', this.id]); // 🔥 ID, не egn
    }
  }

  viewDocuments() {
    if (this.driver && this.driver.id) {
      this.router.navigate(['/driver-documents', this.id]);
    }
  }
}

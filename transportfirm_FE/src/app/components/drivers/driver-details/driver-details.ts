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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef // ✅ за принудително обновяване на view
  ) {}

  ngOnInit() {
    this.loadDriverDetails();
  }

  loadDriverDetails() {
    const egn = this.route.snapshot.paramMap.get('egn');
    if (!egn) {
      this.error = 'Не е посочен ЕГН на шофьора';
      return;
    }

    this.http.get(`http://localhost:8080/api/drivers/${egn}`)
      .pipe(
        catchError(error => {
          this.error = 'Грешка при зареждане на данните за шофьора';
          return of(null);
        })
      )
      .subscribe(data => {
        this.driver = data;
        this.cdr.detectChanges(); // ✅ обновяваме view след като данните са заредени
      });
  }

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
      const expiryDate = new Date(date);
      return expiryDate > today && expiryDate <= in30Days;
    }).length;
  }

  isExpired(dateString: string | null): boolean {
    if (!dateString) return false;
    return new Date(dateString) < new Date();
  }

  isExpiring(dateString: string | null): boolean {
    if (!dateString) return false;
    const expiryDate = new Date(dateString);
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
    
    return expiryDate > today && expiryDate <= in30Days;
  }

  getDocumentStatus(dateString: string | null): string {
    if (!dateString) return 'Няма данни';
    
    if (this.isExpired(dateString)) return 'Изтекъл';
    if (this.isExpiring(dateString)) return 'Изтича скоро';
    
    return 'Валиден';
  }

  formatDate(dateString: string | null): string {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('bg-BG');
  }

  goBack() {
    this.router.navigate(['/drivers']);
  }

  //edit не работи
  editDriver() {
    if (this.driver) {
      this.router.navigate(['/drivers/edit', this.driver.egn]);
    }
  }

  viewDocuments() {
    if (this.driver) {
      this.router.navigate(['/driver-documents', this.driver.egn]);
    }
  }
}

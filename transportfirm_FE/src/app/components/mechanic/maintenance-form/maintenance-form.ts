import {
  Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import {
  MaintenanceService,
  MaintenanceRecord,
  MaintenanceType,
  MaintenanceStatus,
  MaintenanceDocument,
  MaintenanceDocumentType
} from '../../../core/services/maintenance.service';
import { invalidateCache } from '../../../core/interceptors/cache.interceptor';

@Component({
  selector: 'app-maintenance-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './maintenance-form.html',
  styleUrls: ['./maintenance-form.scss']
})
export class MaintenanceFormComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private maintenanceService = inject(MaintenanceService);
  private cdr = inject(ChangeDetectorRef);

  // Route mode: 'new' or 'edit'
  isNew = true;
  vehicleId = '';   // for new
  recordId = '';    // for edit

  loading = true;
  saving = false;
  error = '';
  success = '';

  // Form data
  selectedType: MaintenanceType = 'REPAIR';
  form: Partial<MaintenanceRecord> = {
    type: 'REPAIR',
    status: 'OPEN',
    description: '',
    odometerKmAtService: undefined,
    workshopName: '',
    invoiceNumber: '',
    invoiceDate: undefined,
    partsNet: 0,
    partsVat: 0,
    otherNet: 0,
    otherVat: 0,
  };

  // Documents
  documents: MaintenanceDocument[] = [];
  uploadingDoc = false;
  uploadDocType: MaintenanceDocumentType = 'INVOICE';
  selectedFile: File | null = null;
  docError = '';

  readonly typeOptions: { value: MaintenanceType; label: string }[] = [
    { value: 'REPAIR',      label: 'Ремонт' },
    { value: 'SERVICE',     label: 'Сервиз' },
    { value: 'INSPECTION',  label: 'Технически преглед' },
    { value: 'TIRE_CHANGE', label: 'Смяна на гуми' },
    { value: 'BREAKDOWN',   label: 'Авария' },
    { value: 'ACCIDENT',    label: 'Катастрофа' },
    { value: 'OTHER',       label: 'Друго' },
  ];

  readonly statusOptions: { value: MaintenanceStatus; label: string }[] = [
    { value: 'OPEN',     label: 'Отворен' },
    { value: 'CLOSED',   label: 'Затворен' },
    { value: 'CANCELED', label: 'Отменен' },
  ];

  readonly docTypeOptions: { value: MaintenanceDocumentType; label: string }[] = [
    { value: 'INVOICE', label: 'Фактура' },
    { value: 'PHOTO',   label: 'Снимка' },
    { value: 'REPORT',  label: 'Доклад' },
  ];

  ngOnInit(): void {
    if (!this.isBrowser) return;

    const newVehicleId = this.route.snapshot.paramMap.get('vehicleId');
    const editRecordId = this.route.snapshot.paramMap.get('recordId');

    if (newVehicleId) {
      // New maintenance for a vehicle
      this.isNew = true;
      this.vehicleId = newVehicleId;
      this.loading = false;
    } else if (editRecordId) {
      // Edit existing record
      this.isNew = false;
      this.recordId = editRecordId;
      this.loadRecord(editRecordId);
    } else {
      this.error = 'Невалиден URL.';
      this.loading = false;
    }
  }

  private loadRecord(id: string): void {
    this.maintenanceService.getById(id).subscribe({
      next: (rec) => {
        this.form = {
          type: rec.type,
          status: rec.status,
          description: rec.description ?? '',
          odometerKmAtService: rec.odometerKmAtService,
          workshopName: rec.workshopName ?? '',
          invoiceNumber: rec.invoiceNumber ?? '',
          invoiceDate: rec.invoiceDate,
          partsNet: rec.partsNet ?? 0,
          partsVat: rec.partsVat ?? 0,
          otherNet: rec.otherNet ?? 0,
          otherVat: rec.otherVat ?? 0,
        };
        this.vehicleId = rec.vehicle.id;
        this.loading = false;
        this.loadDocuments(id);
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Грешка при зареждане на записа.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadDocuments(recordId: string): void {
    this.maintenanceService.getDocuments(recordId).subscribe({
      next: (docs) => {
        this.documents = docs;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  save(): void {
    if (this.saving) return;
    this.saving = true;
    this.error = '';
    this.success = '';

    if (this.isNew) {
      // Create record first, then update with form data
      this.maintenanceService.create(this.vehicleId, this.form.type!).subscribe({
        next: (created) => {
          this.recordId = created.id;
          this.isNew = false;
          this.updateRecord(created.id);
        },
        error: () => {
          this.error = 'Грешка при създаване на запис.';
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.updateRecord(this.recordId);
    }
  }

  private updateRecord(recordId: string): void {
    this.maintenanceService.update(recordId, this.form).subscribe({
      next: () => {
        this.success = 'Записът е запазен успешно.';
        this.saving = false;
        invalidateCache();
        if (!this.documents.length) {
          this.loadDocuments(recordId);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Грешка при запазване.';
        this.saving = false;
        this.cdr.detectChanges();
      }
    });
  }

  closeRecord(): void {
    if (!this.recordId || this.saving) return;
    if (!confirm('Затвори записа?')) return;

    this.saving = true;
    this.maintenanceService.close(this.recordId).subscribe({
      next: (updated) => {
        this.form.status = updated.status;
        this.success = 'Записът е затворен.';
        this.saving = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Грешка при затваряне.';
        this.saving = false;
        this.cdr.detectChanges();
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  uploadDocument(): void {
    if (!this.selectedFile || !this.recordId || this.uploadingDoc) return;
    this.uploadingDoc = true;
    this.docError = '';

    this.maintenanceService.uploadDocument(this.recordId, this.selectedFile, this.uploadDocType).subscribe({
      next: (doc) => {
        this.documents.push(doc);
        this.selectedFile = null;
        this.uploadingDoc = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.docError = 'Грешка при качване на документ.';
        this.uploadingDoc = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteDocument(docId: string): void {
    if (!confirm('Изтрий документа?')) return;

    this.maintenanceService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.cdr.detectChanges();
      },
      error: () => alert('Грешка при изтриване.')
    });
  }

  downloadDocument(docId: string, fileName: string): void {
    this.maintenanceService.downloadDocument(docId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Грешка при изтегляне.')
    });
  }

  goBack(): void {
    this.router.navigate(['/mechanic']);
  }
}

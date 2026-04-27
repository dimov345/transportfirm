import {
  Component,
  OnInit,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
  Inject,
  PLATFORM_ID,
  DestroyRef,
  inject
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../../core/auth/auth.service';
import { DriverService } from '../../../core/services/driver.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { DriverDocument } from '../../../core/models/driver/driver-document.model';

interface ExpiryItem {
  label: string;
  issued: string | null;
  expires: string | null;
}

@Component({
  selector: 'app-driver-documents',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './driver-documents.html',
  styleUrls: ['./driver-documents.scss']
})
export class DriverDocumentsComponent implements OnInit {
  employeeId!: string;
  documents: DriverDocument[] = [];
  selectedType: string = '';

  isLoading  = true;
  isUploading = false;

  /** True when the logged-in user is a DRIVER viewing their own docs (read-only mode). */
  isReadOnly = false;

  /** Driver expiry-date panel — populated only in read-only mode (DRIVER viewing self). */
  expiryItems: ExpiryItem[] = [];

  readonly isBrowser: boolean;
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private driverService: DriverService,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) { this.isLoading = false; return; }

    const isDriver = this.auth.hasRole('DRIVER');
    this.isReadOnly = isDriver;

    if (isDriver) {
      // DRIVER role: always load their OWN documents via /employees/me
      this.employeeService.getMe().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: emp => {
          this.employeeId = emp.id;
          this.buildExpiryItems(emp);
          this.loadDocuments();
        },
        error: () => { this.isLoading = false; this.cdr.detectChanges(); }
      });
    } else {
      // ADMIN / MANAGER / DISPATCHER: load by route param id
      const idParam = this.route.snapshot.paramMap.get('id');
      if (!idParam?.trim()) {
        console.error('Invalid employeeId in route');
        this.isLoading = false;
        this.cdr.detectChanges();
        return;
      }
      this.employeeId = idParam;
      this.loadDocuments();
    }
  }

  loadDocuments(): void {
    this.isLoading = true;

    this.driverService.getDocuments(this.employeeId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (docs: DriverDocument[]) => {
        this.documents = docs ?? [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Load documents error:', err);
        this.documents = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }


  onFileSelected(event: Event): void {

    if (!this.isBrowser) return;

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const type = (this.selectedType || '').trim();

    if (!type) {
      alert('Моля, изберете тип документ!');
      input.value = '';
      return;
    }

    if (file.type !== 'application/pdf') {
      alert('Моля, качете само PDF файл!');
      input.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('Файлът е твърде голям (макс. 5 MB)');
      input.value = '';
      return;
    }

    this.isUploading = true;
    this.cdr.detectChanges();

    this.driverService.uploadDocument(this.employeeId, type, file).subscribe({
      next: (doc: DriverDocument) => {
        this.documents = [doc, ...this.documents];
        this.isUploading = false;
        input.value = '';
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Upload error:', err);
        alert('Грешка при качване!');
        this.isUploading = false;
        input.value = '';
        this.cdr.detectChanges();
      }
    });
  }


  download(doc: DriverDocument): void {
    if (!this.isBrowser) return;

    this.driverService.downloadDocument(doc.id).subscribe({
      next: (blob: Blob) => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: (err: unknown) => {
        console.error('Download error:', err);
        alert('Грешка при сваляне!');
      }
    });
  }

  private saveBlob(blob: Blob, fileName: string): void {

    if (!this.isBrowser) return;

    const url = window.URL.createObjectURL(blob);
    const a = window.document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  /** Map Employee.driverInfo expiry dates into a display array for the read-only panel. */
  private buildExpiryItems(emp: any): void {
    const di = emp.driverInfo;
    if (!di) { this.expiryItems = []; return; }

    this.expiryItems = [
      { label: 'Шофьорска книжка',    issued: di.driverLicenseIssuedOn,      expires: di.driverLicenseExpiresOn },
      { label: 'Професионална карта', issued: di.qualificationCardIssuedOn,  expires: di.qualificationCardExpiresOn },
      { label: 'Психо удостоверение', issued: di.psychologicalExamIssuedOn,  expires: di.psychologicalExamExpiresOn },
      { label: 'Дигитална карта',     issued: di.digitalCardIssuedOn,        expires: di.digitalCardExpiresOn }
    ];
  }

  formatDate(d: string | null): string {
    return d ? new Date(d).toLocaleDateString('bg-BG') : '—';
  }

  /** 'expired' | 'soon' (<=30d) | 'ok' | 'none' — drives left-border color on the expiry row. */
  expiryStatus(d: string | null): 'expired' | 'soon' | 'ok' | 'none' {
    if (!d) return 'none';
    const exp = new Date(d).getTime();
    const now = Date.now();
    if (exp < now) return 'expired';
    if (exp - now < 30 * 24 * 60 * 60 * 1000) return 'soon';
    return 'ok';
  }

  delete(docId: string): void {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.driverService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Delete error:', err);
        alert('Грешка при изтриване на документа!');
      }
    });
  }
}

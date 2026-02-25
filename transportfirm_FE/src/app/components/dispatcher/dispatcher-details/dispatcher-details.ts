import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { catchError, of } from 'rxjs';

import { DispatcherService } from '../../../core/services/dispatcher.service';
import { Employee } from '../../../core/models/employee/employee.model';
import { TruckGroup } from '../../../core/services/truck-group.service';
import { EmployeeDocument } from '../../../core/models/employee/employee-document.model';

@Component({
  selector: 'app-dispatcher-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dispatcher-details.html',
  styleUrls: ['./dispatcher-details.scss']
})
export class DispatcherDetailsComponent implements OnInit {
  employee: Employee | null = null;
  groups: TruckGroup[] = [];
  documents: EmployeeDocument[] = [];

  loading = true;
  loadingDocs = false;
  isUploading = false;
  error = '';

  selectedDocType = '';
  employeeId!: string;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dispatcherService: DispatcherService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.isBrowser) return;

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Не е посочен ID';
      this.loading = false;
      return;
    }

    this.employeeId = id;
    this.loadData();
  }

  private loadData() {
    this.loading = true;
    this.error = '';

    this.dispatcherService.getEmployee(this.employeeId).pipe(
      catchError((err: unknown) => {
        console.error(err);
        this.error = 'Грешка при зареждане на данните';
        this.loading = false;
        this.cdr.detectChanges();
        return of(null);
      })
    ).subscribe(emp => {
      if (!emp) return;

      this.employee = emp;

      const dispatcherInfoId = emp.dispatcherInfo?.id;
      if (dispatcherInfoId) {
        this.loadGroups(dispatcherInfoId);
      }

      this.loadDocuments();
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  private loadGroups(dispatcherInfoId: string) {
    this.dispatcherService.getGroupsByDispatcher(dispatcherInfoId).subscribe({
      next: groups => {
        this.groups = groups;
        this.cdr.detectChanges();
      },
      error: () => {
        this.groups = [];
        this.cdr.detectChanges();
      }
    });
  }

  loadDocuments() {
    this.loadingDocs = true;
    this.dispatcherService.getDocuments(this.employeeId).subscribe({
      next: docs => {
        this.documents = docs ?? [];
        this.loadingDocs = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.documents = [];
        this.loadingDocs = false;
        this.cdr.detectChanges();
      }
    });
  }

  onFileSelected(event: Event) {
    if (!this.isBrowser) return;

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const type = this.selectedDocType.trim();
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

    this.dispatcherService.uploadDocument(this.employeeId, type, file).subscribe({
      next: doc => {
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

  downloadDocument(doc: EmployeeDocument) {
    if (!this.isBrowser) return;

    this.dispatcherService.downloadDocument(doc.id).subscribe({
      next: blob => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: () => alert('Грешка при сваляне!')
    });
  }

  private saveBlob(blob: Blob, fileName: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  deleteDocument(docId: string) {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.dispatcherService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.cdr.detectChanges();
      },
      error: () => alert('Грешка при изтриване!')
    });
  }

  goBack() {
    this.router.navigate(['/dispatchers']);
  }

  getStatusLabel(status: string | null): string {
    const map: Record<string, string> = {
      ACTIVE: 'Активен',
      INACTIVE: 'Неактивен',
      FIRED: 'Уволнен',
      ON_LEAVE: 'В отпуск'
    };
    return status ? (map[status] ?? status) : '—';
  }

  getStatusClass(status: string | null): string {
    const map: Record<string, string> = {
      ACTIVE: 'status-active',
      INACTIVE: 'status-inactive',
      FIRED: 'status-fired',
      ON_LEAVE: 'status-leave'
    };
    return status ? (map[status] ?? '') : '';
  }

  formatDate(date: string | null): string {
    return date ? new Date(date).toLocaleDateString('bg-BG') : '—';
  }
}

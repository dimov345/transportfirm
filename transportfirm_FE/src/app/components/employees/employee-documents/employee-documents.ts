import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { EmployeeDocumentService } from '../../../core/services/employee-document.service';
import { EmployeeDocument } from '../../../core/models/employee/employee-document.model';

@Component({
  selector: 'app-employee-documents',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './employee-documents.html',
  styleUrls: ['./employee-documents.scss']
})
export class EmployeeDocuments implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(EmployeeDocumentService);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  employeeId!: string;
  documents: EmployeeDocument[] = [];

  selectedType: string = '';

  isLoading = true;
  isUploading = false;

  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    // ✅ SSR guard: не правим HTTP / browser APIs на сървъра
    if (!this.isBrowser) return;

    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.errorMessage = 'Невалиден employeeId.';
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.employeeId = idParam;
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.detectChanges();

    this.service.getDocuments(this.employeeId).subscribe({
      next: (docs) => {
        this.documents = docs ?? [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Load documents error:', err);
        this.documents = [];
        this.isLoading = false;

        if (err?.status === 401) {
          this.errorMessage = 'Нямаш активна сесия. Влез отново.';
        } else {
          this.errorMessage = err?.error?.message || 'Грешка при зареждане на документи.';
        }

        this.cdr.detectChanges();
      }
    });
  }

  onFileSelected(event: Event): void {
    if (!this.isBrowser) return;

    this.errorMessage = '';
    this.successMessage = '';

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const type = (this.selectedType || '').trim();

    if (!type) {
      this.errorMessage = 'Моля, изберете тип документ!';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    if (file.type !== 'application/pdf') {
      this.errorMessage = 'Моля, качете само PDF файл!';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'Файлът е твърде голям (макс. 5 MB).';
      input.value = '';
      this.cdr.detectChanges();
      return;
    }

    this.isUploading = true;
    this.cdr.detectChanges();

    this.service.uploadDocument(this.employeeId, type, file).subscribe({
      next: (doc) => {
        this.documents = [doc, ...this.documents];
        this.isUploading = false;
        this.successMessage = 'Документът е качен успешно.';
        input.value = '';
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Upload error:', err);
        this.isUploading = false;
        input.value = '';

        if (err?.status === 401) {
          this.errorMessage = 'Нямаш активна сесия. Влез отново.';
        } else {
          this.errorMessage = err?.error?.message || 'Грешка при качване.';
        }

        this.cdr.detectChanges();
      }
    });
  }

  download(doc: EmployeeDocument): void {
    if (!this.isBrowser) return;

    this.errorMessage = '';
    this.successMessage = '';

    this.service.downloadDocument(doc.id).subscribe({
      next: (blob: Blob) => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: (err: any) => {
        console.error('Download error:', err);
        this.errorMessage = err?.error?.message || 'Грешка при сваляне.';
        this.cdr.detectChanges();
      }
    });
  }

  deleteDoc(docId: string): void {
    if (!this.isBrowser) return;

    this.errorMessage = '';
    this.successMessage = '';

    // ✅ confirm само в браузъра
    const ok = window.confirm('Наистина ли искате да изтриете този документ?');
    if (!ok) return;

    this.service.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.successMessage = 'Документът е изтрит.';
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Delete error:', err);
        this.errorMessage = err?.error?.message || 'Грешка при изтриване.';
        this.cdr.detectChanges();
      }
    });
  }

  private saveBlob(blob: Blob, fileName: string): void {
    if (!this.isBrowser) return;

    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }
}

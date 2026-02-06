import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DriverService } from '../../../core/services/driver.service';
import { DriverDocument } from '../../../core/models/driver/driver-document.model';

@Component({
  selector: 'app-driver-documents',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './driver-documents.html',
  styleUrls: ['./driver-documents.scss']
})
export class DriverDocumentsComponent implements OnInit {
  employeeId!: string; // ✅ UUID
  documents: DriverDocument[] = [];

  selectedType: string = '';

  isLoading = true;
  isUploading = false;

  constructor(
    private route: ActivatedRoute,
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam || !idParam.trim()) {
      console.error('Invalid employeeId in route');
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.employeeId = idParam; // ✅ UUID е string
    this.loadDocuments();
  }

  /** Зареждане на документи (по employeeId) */
  loadDocuments(): void {
    this.isLoading = true;

    this.driverService.getDocuments(this.employeeId).subscribe({
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

  /** Качване */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    // махаме празните/грешни option-и с интервали
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

  /** 📥 Изтегляне (по docId) */
  download(doc: DriverDocument): void {
    this.driverService.downloadDocument(doc.id).subscribe({
      next: (blob: Blob) => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: (err: unknown) => {
        console.error('Download error:', err);
        alert('Грешка при сваляне!');
      }
    });
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }

  /** 🗑️ Изтриване (по docId) */
  delete(docId: string): void { // ✅ UUID
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

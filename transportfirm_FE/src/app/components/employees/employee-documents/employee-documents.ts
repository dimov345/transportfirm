import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  employeeId!: string; // ✅ UUID
  documents: EmployeeDocument[] = [];

  selectedType: string = '';

  isLoading = true;
  isUploading = false;

  constructor(
    private route: ActivatedRoute,
    private service: EmployeeDocumentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      console.error('Invalid employeeId');
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.employeeId = idParam; // ✅ БЕЗ Number(), UUID си е string
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;

    this.service.getDocuments(this.employeeId).subscribe({
      next: (docs) => {
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

    this.service.uploadDocument(this.employeeId, type, file).subscribe({
      next: (doc) => {
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

  download(doc: EmployeeDocument): void {
    this.service.downloadDocument(doc.id).subscribe({
      next: (blob: Blob) => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: (err: unknown) => {
        console.error('Download error:', err);
        alert('Грешка при сваляне!');
      }
    });
  }

  Delete(docId: string): void { 
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.service.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Delete error:', err);
        alert('Грешка при изтриване!');
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
}

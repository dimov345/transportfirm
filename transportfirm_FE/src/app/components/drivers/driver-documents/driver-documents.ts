import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DriverService, DriverDocument } from '../../../core/services/driver.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-driver-documents',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './driver-documents.html',
  styleUrls: ['./driver-documents.scss']
})
export class DriverDocumentsComponent implements OnInit {
  id!: number; // driverId, НЕ egn
  documents: DriverDocument[] = [];

  selectedType = '';
  
  isLoading = true;
  isUploading = false;

  constructor(
    private route: ActivatedRoute,
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id')); // 👈 id от URL
    this.loadDocuments();
  }

  /** Зареждане на документи */
  loadDocuments() {
    this.isLoading = true;

    this.driverService.getDocuments(this.id).subscribe({
      next: docs => {
        this.documents = docs;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /** Качване */
 onFileSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (!this.selectedType) {
    alert("Моля, изберете тип документ!");
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

  this.driverService.uploadDocument(this.id, this.selectedType, file).subscribe({
    next: (doc: any) => {
      this.documents.push(doc);
      this.isUploading = false;
      input.value = '';
      this.cdr.detectChanges();
    },
    error: () => {
      alert('Грешка при качване!');
      this.isUploading = false;
      input.value = '';
      this.cdr.detectChanges();
    }
  });
}


  /** 📥 Изтегляне */
  download(docId: number, fileName: string) {
    this.driverService.downloadDocument(docId).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  /** 🗑️ Изтриване */
  delete(docId: number) {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.driverService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.forceUpdate();
      },
      error: () => alert('Грешка при изтриване на документа!')
    });
  }

  private forceUpdate() {
    setTimeout(() => this.cdr.detectChanges(), 0);
  }
}

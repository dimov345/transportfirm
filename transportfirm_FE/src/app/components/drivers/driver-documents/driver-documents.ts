import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DriverService, DriverDocument } from '../../../core/services/driver';

@Component({
  selector: 'app-driver-documents',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './driver-documents.html',
  styleUrls: ['./driver-documents.scss']
})
export class DriverDocumentsComponent implements OnInit {
  egn!: string;
  documents: DriverDocument[] = [];
  isLoading = false;
  isUploading = false;

  constructor(
    private route: ActivatedRoute, 
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.egn = this.route.snapshot.paramMap.get('egn')!;
    this.loadDocuments();
  }

  /** Зарежда всички документи за даден шофьор */
  loadDocuments() {
    this.isLoading = true;
    this.driverService.getDocuments(this.egn).subscribe({
      next: docs => {
        this.documents = [...docs]; // Винаги създаваме нов масив
        this.isLoading = false;
        this.forceUpdate();
      },
      error: () => {
        this.isLoading = false;
        this.forceUpdate();
      }
    });
  }

  /** Качва избрания файл веднага */
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    // Проверки
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
    this.forceUpdate();

    // Качване веднага
    this.driverService.uploadDocument(this.egn, file).subscribe({
      next: (newDocument) => {
        // Създаваме напълно нов масив
        this.documents = [...this.documents, newDocument];
        this.isUploading = false;
        input.value = '';
        this.forceUpdate();
      },
      error: () => {
        alert('Грешка при качване на документа!');
        input.value = '';
        this.isUploading = false;
        this.forceUpdate();
      }
    });
  }

  /** Изтегляне */
  download(id: number, fileName: string) {
    this.driverService.downloadDocument(this.egn, id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Изтриване */
  delete(id: number) {
    if (confirm('Наистина ли искате да изтриете този документ?')) {
      this.driverService.deleteDocument(this.egn, id).subscribe({
        next: () => {
          // Създаваме напълно нов масив
          this.documents = this.documents.filter(doc => doc.id !== id);
          this.forceUpdate();
        },
        error: () => {
          alert('Грешка при изтриване на документа!');
        }
      });
    }
  }

  /** Принудително обновяване на view */
  private forceUpdate() {
    setTimeout(() => {
      this.cdr.detectChanges();
    }, 0);
  }
}
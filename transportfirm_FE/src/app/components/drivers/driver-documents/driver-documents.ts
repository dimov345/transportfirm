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
  
  isLoading = true;     // ✔ започваме като loading → без премигване
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
        this.documents = [...docs];
        this.isLoading = false;
        this.forceUpdate();
      },
      error: () => {
        this.isLoading = false;
        this.forceUpdate();
      }
    });
  }

  /** Качване */
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

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

    this.driverService.uploadDocument(this.egn, file).subscribe({
      next: doc => {
        this.documents = [...this.documents, doc];
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
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Изтриване */
  delete(id: number) {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.driverService.deleteDocument(this.egn, id).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== id);
        this.forceUpdate();
      },
      error: () => {
        alert('Грешка при изтриване на документа!');
      }
    });
  }

  /** Принудително обновяване на view */
  private forceUpdate() {
    setTimeout(() => {
      this.cdr.detectChanges();
    }, 0);
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { VehicleService, VehicleDocument } from '../../../core/services/vehicle';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-vehicle-documents',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vehicle-documents.html',
  styleUrls: ['./vehicle-documents.scss']
})
export class VehicleDocumentsComponent implements OnInit {
  plateNumber!: string;
  documents: VehicleDocument[] = [];
  
  isLoading = true;
  isUploading = false;

  constructor(
    private route: ActivatedRoute,
    private vehicleService: VehicleService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.plateNumber = this.route.snapshot.paramMap.get('plateNumber')!;
    this.loadDocuments();
  }

  /** Зарежда всички документи за дадено ППС */
  loadDocuments() {
    this.isLoading = true;

    this.vehicleService.getDocuments(this.plateNumber).subscribe({
      next: (docs) => {
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

  /** Качване на документ */
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

    this.vehicleService.uploadDocument(this.plateNumber, file).subscribe({
      next: (newDocument) => {
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

  /** Изтегляне на документ */
  download(id: number, fileName: string) {
    this.vehicleService.downloadDocument(this.plateNumber, id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Изтриване на документ */
  delete(id: number) {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.vehicleService.deleteDocument(this.plateNumber, id).subscribe({
      next: () => {
        this.documents = this.documents.filter(doc => doc.id !== id);
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
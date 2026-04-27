import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehicleService, VehicleDocument, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-vehicle-documents',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './vehicle-documents.html',
  styleUrls: ['./vehicle-documents.scss']
})
export class VehicleDocumentsComponent implements OnInit {
  id = '';                 // UUID от route
  plateNumber = '';        // само за показване в UI

  documents: VehicleDocument[] = [];
  selectedType: string = '';

  isLoading = true;
  isUploading = false;

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private vehicleService: VehicleService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (!this.id) {
      this.isLoading = false;
      return;
    }

    this.loadVehicleHeader(); // за да покажем номер (по желание)
    this.loadDocuments();
  }

  /** Зарежда данните за ППС (само за header: plateNumber) */
  loadVehicleHeader() {
    this.vehicleService.getById(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (vehicle: VehicleInfo) => {
        this.plateNumber = vehicle.plateNumber || '';
        this.forceUpdate();
      },
      error: () => {
        // не е фатално – документите пак може да се покажат
        this.plateNumber = '';
        this.forceUpdate();
      }
    });
  }

  /** Зарежда всички документи за дадено ППС (по UUID) */
  loadDocuments() {
    this.isLoading = true;

    this.vehicleService.getDocuments(this.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    const type = (this.selectedType || '').trim();

    if (!file) return;

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
    this.forceUpdate();

    this.vehicleService.uploadDocument(this.id, this.selectedType, file).subscribe({
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
  download(id: string, fileName: string) {
    this.vehicleService.downloadDocument(id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Изтриване на документ */
  delete(id: string) {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;

    this.vehicleService.deleteDocument(id).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== id);
        this.forceUpdate();
      },
      error: () => {
        alert('Грешка при изтриване на документа!');
        this.forceUpdate();
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

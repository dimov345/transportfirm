import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { catchError, forkJoin, of } from 'rxjs';

import { MechanicService } from '../../../core/services/mechanic.service';
import { Employee } from '../../../core/models/employee/employee.model';
import { TruckGroup } from '../../../core/services/truck-group.service';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { EmployeeDocumentService } from '../../../core/services/employee-document.service';
import { EmployeeDocument } from '../../../core/models/employee/employee-document.model';

@Component({
  selector: 'app-mechanic-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './mechanic-details.html',
  styleUrls: ['./mechanic-details.scss']
})
export class MechanicDetailsComponent implements OnInit {
  employee: Employee | null = null;
  groups: TruckGroup[] = [];
  groupVehicles = new Map<string, VehicleInfo[]>();

  loading = true;
  error = '';

  employeeId!: string;

  // Documents
  documents: EmployeeDocument[] = [];
  loadingDocs = false;
  isUploading = false;
  selectedDocType = '';

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private mechanicService: MechanicService,
    private vehicleService: VehicleService,
    private employeeDocumentService: EmployeeDocumentService,
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
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loadingDocs = true;
    this.employeeDocumentService.getDocuments(this.employeeId).subscribe({
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const type = (this.selectedDocType || '').trim();
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

    this.employeeDocumentService.uploadDocument(this.employeeId, type, file).subscribe({
      next: doc => {
        this.documents = [doc, ...this.documents];
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

  downloadDocument(doc: EmployeeDocument): void {
    this.employeeDocumentService.downloadDocument(doc.id).subscribe({
      next: blob => this.saveBlob(blob, doc.fileName || 'document.pdf'),
      error: () => alert('Грешка при сваляне!')
    });
  }

  deleteDocument(docId: string): void {
    if (!confirm('Наистина ли искате да изтриете този документ?')) return;
    this.employeeDocumentService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
        this.cdr.detectChanges();
      },
      error: () => alert('Грешка при изтриване!')
    });
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = window.document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private loadData() {
    this.loading = true;
    this.error = '';
    this.groups = [];
    this.groupVehicles = new Map();

    this.mechanicService.getEmployee(this.employeeId).pipe(
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

      const mechanicInfoId = emp.mechanicInfo?.id;
      if (mechanicInfoId) {
        this.loadGroups(mechanicInfoId);
      }

      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  private loadGroups(mechanicInfoId: string) {
    this.mechanicService.getGroupsByMechanic(mechanicInfoId).subscribe({
      next: groups => {
        this.groups = groups;
        this.cdr.detectChanges();
        this.loadVehiclesForGroups(groups);
      },
      error: () => {
        this.groups = [];
        this.cdr.detectChanges();
      }
    });
  }

  private loadVehiclesForGroups(groups: TruckGroup[]) {
    if (groups.length === 0) return;
    const requests = groups.map(g =>
      this.vehicleService.getByGroup(g.id).pipe(catchError(() => of([] as VehicleInfo[])))
    );
    forkJoin(requests).subscribe(results => {
      results.forEach((vehicles, i) => this.groupVehicles.set(groups[i].id, vehicles));
      this.cdr.detectChanges();
    });
  }

  goToVehicle(vehicleId: string) {
    this.router.navigate(['/vehicles', vehicleId]);
  }

  goBack() {
    this.router.navigate(['/mechanics']);
  }

  goToEdit() {
    this.router.navigate(['/employees/edit', this.employeeId]);
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

import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeeService, Employee } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/auth/auth.service';

import { FormsModule } from '@angular/forms';
import { TruckGroup, TruckGroupService, GroupType } from '../../../core/services/truck-group.service';
import { GroupVehicleAssignment } from '../../assignment/group-vehicle-assignment/group-vehicle-assignment';

@Component({
  selector: 'app-employee-details',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterModule, FormsModule, GroupVehicleAssignment],
  templateUrl: './employee-details.html',
  styleUrls: ['./employee-details.scss']
})
export class EmployeeDetails implements OnInit {

  employee: Employee | null = null;
  loading = true;
  errorMessage = '';

  get isAdmin():    boolean { return this.auth.hasRole('ADMIN'); }
  get backRoute():  string  { return this.isAdmin ? '/employees' : '/manager-staff'; }

  // UI state
  showCreateGroup = false;
  showAssignVehicles = false;
  groupName = '';

  // ако бекенд DTO-то не връща group вътре в employee,
  // пазим я локално след create
  createdGroup: TruckGroup | null = null;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
    private auth: AuthService,
    private groups: TruckGroupService
  ) {}

  ngOnInit(): void {
    if (!this.isBrowser) return;

    const id = this.route.snapshot.paramMap.get('id');
    if (!id || !id.trim()) {
      this.router.navigate([this.backRoute]);
      return;
    }

    this.loadEmployee(id);
    
  }

  private loadEmployee(id: string): void {
    this.loading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.employeeService.getById(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.employee = data;
        this.loading = false;
        this.loadGroupForEmployee();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Грешка при зареждане на служител';
        this.loadGroupForEmployee();
        this.cdr.detectChanges();
      }
    });
  }

  private loadGroupForEmployee(): void {
  this.createdGroup = null;

  if (!this.employee) return;

  const groupType = this.getGroupTypeValue();
  if (!groupType) return;

  if (groupType === 'MECHANIC') {
    const mechanicId = this.getMechanicInfoId();
    if (!mechanicId) return;

    this.groups.getGroupsByMechanic(mechanicId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (list) => {
        this.createdGroup = list?.[0] ?? null;
        this.cdr.detectChanges();
      }
    });
  }

  if (groupType === 'DISPATCHER') {
    const dispatcherId = this.getDispatcherInfoId();
    if (!dispatcherId) return;

    this.groups.getGroupsByDispatcher(dispatcherId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (list) => {
        this.createdGroup = list?.[0] ?? null;
        this.cdr.detectChanges();
      }
    });
  }
}


  // ===== Role / type helpers =====

  private getRoleUpper(): string {
    return String((this.employee as any)?.role || '').toUpperCase();
  }

  isMechanic(): boolean {
    return this.getRoleUpper() === 'MECHANIC';
  }

  isDispatcher(): boolean {
    return this.getRoleUpper() === 'DISPATCHER';
  }

  private getMechanicInfoId(): string | null {
    return (this.employee as any)?.mechanicInfo?.id ?? null;
  }

  private getDispatcherInfoId(): string | null {
    return (this.employee as any)?.dispatcherInfo?.id ?? null;
  }

  // ===== GROUP helpers (works with multiple DTO shapes) =====

  private pickGroupObj(emp: any): any | null {
    if (!emp) return null;

    return (
      // локално създадена група (ако DTO-то не я връща)
      this.createdGroup ||

      emp?.dispatcherInfo?.truckGroup ||
      emp?.dispatcherInfo?.group ||
      emp?.dispatcherInfo?.dispatcherGroup ||

      emp?.mechanicInfo?.truckGroup ||
      emp?.mechanicInfo?.group ||
      emp?.mechanicInfo?.mechanicGroup ||

      emp?.truckGroup ||
      emp?.group ||
      null
    );
  }

  hasGroup(): boolean {
    return !!this.pickGroupObj(this.employee);
  }

  getGroupName(): string {
    const g = this.pickGroupObj(this.employee);
    return g?.groupName || g?.name || '—';
  }

  getGroupId(): string {
    const g = this.pickGroupObj(this.employee);
    return g?.id || '—';
  }

  getGroupTypeLabel(): string {
    const emp: any = this.employee;
    if (!emp) return '—';

    if (this.isDispatcher()) return 'Диспечерска група';
    if (this.isMechanic()) return 'Механик група';

    // fallback ако role не е сетнат
    if (emp?.dispatcherInfo) return 'Диспечерска група';
    if (emp?.mechanicInfo) return 'Механик група';

    return 'Група';
  }

  getGroupTypeValue(): GroupType | null {
    if (this.isMechanic()) return 'MECHANIC';
    if (this.isDispatcher()) return 'DISPATCHER';
    return null;
  }

  // ===== Actions =====

  toggleCreateGroup(): void {
    this.showCreateGroup = !this.showCreateGroup;
    this.groupName = '';
    this.errorMessage = '';
  }

  toggleAssignVehicles(): void {
    this.showAssignVehicles = !this.showAssignVehicles;
  }

  createGroup(): void {
    if (!this.employee) return;

    const name = (this.groupName || '').trim();
    if (!name) {
      this.errorMessage = 'Моля въведи име на група.';
      return;
    }

    const groupType = this.getGroupTypeValue();
    if (!groupType) {
      this.errorMessage = 'Този служител не е механик/диспечер и не може да има група.';
      return;
    }

    this.errorMessage = '';

    if (groupType === 'MECHANIC') {
      const mechanicId = this.getMechanicInfoId();
      if (!mechanicId) {
        this.errorMessage = 'Липсва mechanicInfoId (провери DTO-то от бекенда).';
        return;
      }

      this.groups.createMechanicGroup(mechanicId, name).subscribe({
        next: (g) => {
          this.createdGroup = g;
          this.showCreateGroup = false;
          this.showAssignVehicles = true; // удобно: веднага да assign-ваш
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.errorMessage = err?.error?.message || 'Грешка при създаване на група.';
          this.cdr.markForCheck();
        }
      });

      return;
    }

    if (groupType === 'DISPATCHER') {
      const dispatcherId = this.getDispatcherInfoId();
      if (!dispatcherId) {
        this.errorMessage = 'Липсва dispatcherInfoId (провери DTO-то от бекенда).';
        return;
      }

      this.groups.createDispatcherGroup(dispatcherId, name).subscribe({
        next: (g) => {
          this.createdGroup = g;
          this.showCreateGroup = false;
          this.showAssignVehicles = true;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.errorMessage = err?.error?.message || 'Грешка при създаване на група.';
          this.cdr.markForCheck();
        }
      });
    }
  }

  readonly jobTitleLabels: Record<string, string> = {
    DRIVER: 'Шофьор', MECHANIC: 'Механик', DISPATCHER: 'Спедитор',
    ACCOUNTANT: 'Счетоводител', MANAGER: 'Мениджър',
    ADMINISTRATOR: 'Администратор'
  };

  readonly employmentStatusLabels: Record<string, string> = {
    ACTIVE: 'Активен', ON_PROBATION: 'Изпитателен срок', ON_LEAVE: 'В отпуск',
    SICK: 'Болничен', SUSPENDED: 'Временно уволнен', FIRED: 'Уволнен', RETIRED: 'Пенсиониран'
  };

  readonly contractTypeLabels: Record<string, string> = {
    FULL_TIME: 'Пълен работен ден', PART_TIME: 'Непълен работен ден',
    TEMPORARY: 'Временен', INTERNSHIP: 'Стаж', CIVIL_CONTRACT: 'Граждански договор'
  };

  translateLabel(map: Record<string, string>, value: string | null | undefined): string {
    if (!value) return '—';
    return map[value] ?? value;
  }

  private readonly numFmt = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  fmtEur(eur: number | null | undefined): string {
    return '€' + this.numFmt.format(eur ?? 0);
  }

  editEmployee(employeeId: string) {
    if (!this.employee) return;
    this.router.navigate(['/employees/edit', employeeId]);
  }

  openDocuments() {
    if (!this.employee) return;
    this.router.navigate(['/employees-documents', this.employee.id]);
  }

  back() {
    this.router.navigate([this.backRoute]);
  }
}

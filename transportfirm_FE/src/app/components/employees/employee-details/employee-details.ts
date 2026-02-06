import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService, Employee } from '../../../core/services/employee.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-employee-details',
  imports: [RouterModule],
  templateUrl: './employee-details.html',
  styleUrls: ['./employee-details.scss']
})
export class EmployeeDetails implements OnInit {

  employee: Employee | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id || !id.trim()) {
      this.router.navigate(['/employees']);
      return;
    }

    this.employeeService.getById(id).subscribe({
      next: (data) => {
        this.employee = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  editEmployee(employeeId: string) {
    if (!this.employee) return;
    this.router.navigate(['/employees/edit', employeeId]);
  }

  openDocuments() {
    if (!this.employee) return;
    this.router.navigate(['/employees', this.employee.id, 'documents']);
  }

  back() {
    this.router.navigate(['/employees']);
  }
}

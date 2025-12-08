import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService, Employee } from '../../../core/services/employee';
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
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    const id = param ? Number(param) : 0;

    if (id <= 0) {
      this.router.navigate(['/employees']);
      return;
    }

    this.employeeService.getById(id).subscribe({
      next: (data) => {
        this.employee = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  editEmployee() {
    if (!this.employee) return;
    this.router.navigate(['/employees/edit', this.employee.id]);
  }

  openDocuments() {
    if (!this.employee) return;
    this.router.navigate(['/employees', this.employee.id, 'documents']);
  }

  back() {
    this.router.navigate(['/employees']);
  }
}

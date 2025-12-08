import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { DriversListComponent } from './components/drivers/drivers-list/drivers-list';
import { DriverFormComponent } from './components/drivers/driver-form/driver-form';
import { DriverDetails } from './components/drivers/driver-details/driver-details';
import { DriverDocumentsComponent } from './components/drivers/driver-documents/driver-documents';

import { VehicleListComponent } from './components/vehicles/vehicle-list/vehicle-list';
import { VehicleFormComponent } from './components/vehicles/vehicle-form/vehicle-form';
import { VehicleDetails } from './components/vehicles/vehicle-details/vehicle-details';
import { VehicleDocumentsComponent } from './components/vehicles/vehicle-documents/vehicle-documents';

import { EmployeeList } from './components/employees/employee-list/employee-list';
import { EmployeeForm } from './components/employees/employee-form/employee-form';
import { EmployeeDetails } from './components/employees/employee-details/employee-details';
import { EmployeeDocuments } from './components/employees/employee-documents/employee-documents';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'drivers', component: DriversListComponent },
  { path: 'drivers/new', component: DriverFormComponent },
  { path: 'drivers/edit/:id', component: DriverFormComponent },
  { path: 'drivers/:id', component: DriverDetails },
  { path: 'driver-documents/:id', component: DriverDocumentsComponent },

  { path: 'vehicles', component: VehicleListComponent },
  { path: 'vehicles/new', component: VehicleFormComponent },
  { path: 'vehicles/edit/:plateNumber', component: VehicleFormComponent },
  { path: 'vehicles/:plateNumber', component: VehicleDetails },
  { path: 'vehicles-documents/:plateNumber', component: VehicleDocumentsComponent },

  { path: 'employees', component: EmployeeList },
  { path: 'employees/new', component: EmployeeForm },
  { path: 'employees/edit/:id', component: EmployeeForm },
  { path: 'employees/details/:id', component: EmployeeDetails },
  { path: 'employees-documents/:id', component: EmployeeDocuments },
  { path: '**', redirectTo: '' }
];

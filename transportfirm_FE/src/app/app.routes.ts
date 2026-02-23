import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { authGuard } from './core/auth/auth.guard';

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
import { FirstLogin } from './pages/first-login/first-login';
import { MechanicDashboardComponent } from './components/mechanic/mechanic-dashboard/mechanic-dashboard';
import { MaintenanceFormComponent } from './components/mechanic/maintenance-form/maintenance-form';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'first-login', component: FirstLogin },

  { path: '', component: Home, canActivate: [authGuard] },

  { path: 'drivers', component: DriversListComponent, canActivate: [authGuard] },
  { path: 'drivers/new', component: DriverFormComponent, canActivate: [authGuard] },
  { path: 'drivers/edit/:id', component: DriverFormComponent, canActivate: [authGuard] },
  { path: 'drivers/:id', component: DriverDetails, canActivate: [authGuard] },
  { path: 'driver-documents/:id', component: DriverDocumentsComponent, canActivate: [authGuard] },

  { path: 'vehicles', component: VehicleListComponent, canActivate: [authGuard] },
  { path: 'vehicles/new', component: VehicleFormComponent, canActivate: [authGuard] },
  { path: 'vehicles/edit/:id', component: VehicleFormComponent, canActivate: [authGuard] },
  { path: 'vehicles/:id', component: VehicleDetails, canActivate: [authGuard] },
  { path: 'vehicles-documents/:id', component: VehicleDocumentsComponent, canActivate: [authGuard] },

  { path: 'employees', component: EmployeeList, canActivate: [authGuard] },
  { path: 'employees/new', component: EmployeeForm, canActivate: [authGuard] },
  { path: 'employees/edit/:id', component: EmployeeForm, canActivate: [authGuard] },
  { path: 'employees/details/:id', component: EmployeeDetails, canActivate: [authGuard] },
  { path: 'employees-documents/:id', component: EmployeeDocuments, canActivate: [authGuard] },

  { path: 'mechanic', component: MechanicDashboardComponent, canActivate: [authGuard] },
  { path: 'mechanic/maintenance/new/:vehicleId', component: MaintenanceFormComponent, canActivate: [authGuard] },
  { path: 'mechanic/maintenance/edit/:recordId', component: MaintenanceFormComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '' }
];

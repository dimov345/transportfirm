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
import { MechanicListComponent } from './components/mechanic/mechanic-list/mechanic-list';
import { MechanicDetailsComponent } from './components/mechanic/mechanic-details/mechanic-details';

import { DispatcherListComponent } from './components/dispatcher/dispatcher-list/dispatcher-list';
import { DispatcherDetailsComponent } from './components/dispatcher/dispatcher-details/dispatcher-details';
import { DispatcherDashboardComponent } from './components/dispatcher/dispatcher-dashboard/dispatcher-dashboard';

import { ProfileComponent } from './pages/profile/profile';

const ADMIN_MANAGER = ['ADMIN', 'MANAGER'];
const ADMIN_MANAGER_DISPATCHER = ['ADMIN', 'MANAGER', 'DISPATCHER'];
const ADMIN_MANAGER_MECHANIC = ['ADMIN', 'MANAGER', 'MECHANIC'];
const ALL_STAFF = ['ADMIN', 'MANAGER', 'DISPATCHER', 'MECHANIC'];

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'first-login', component: FirstLogin },

  { path: '', component: Home, canActivate: [authGuard] },

  // Drivers — ADMIN, MANAGER, DISPATCHER
  { path: 'drivers', component: DriversListComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_DISPATCHER } },
  { path: 'drivers/new', component: DriverFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'drivers/edit/:id', component: DriverFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'drivers/:id', component: DriverDetails, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_DISPATCHER } },
  { path: 'driver-documents/:id', component: DriverDocumentsComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_DISPATCHER } },

  // Vehicles — all staff can view
  { path: 'vehicles', component: VehicleListComponent, canActivate: [authGuard], data: { roles: ALL_STAFF } },
  { path: 'vehicles/new', component: VehicleFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'vehicles/edit/:id', component: VehicleFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'vehicles/:id', component: VehicleDetails, canActivate: [authGuard], data: { roles: ALL_STAFF } },
  { path: 'vehicles-documents/:id', component: VehicleDocumentsComponent, canActivate: [authGuard], data: { roles: ALL_STAFF } },

  // Employees — ADMIN, MANAGER only
  { path: 'employees', component: EmployeeList, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'employees/new', component: EmployeeForm, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'employees/edit/:id', component: EmployeeForm, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'employees/details/:id', component: EmployeeDetails, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'employees-documents/:id', component: EmployeeDocuments, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },

  // Mechanics list/details — ADMIN, MANAGER only
  { path: 'mechanics', component: MechanicListComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'mechanics/:id', component: MechanicDetailsComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },

  // Dispatchers list/details — ADMIN, MANAGER only
  { path: 'dispatchers', component: DispatcherListComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },
  { path: 'dispatchers/:id', component: DispatcherDetailsComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER } },

  // Dispatcher personal dashboard — DISPATCHER role
  { path: 'dispatcher', component: DispatcherDashboardComponent, canActivate: [authGuard], data: { roles: [...ADMIN_MANAGER, 'DISPATCHER'] } },

  // Mechanic personal dashboard — MECHANIC role only
  { path: 'mechanic', component: MechanicDashboardComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_MECHANIC } },
  { path: 'mechanic/maintenance/new/:vehicleId', component: MaintenanceFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_MECHANIC } },
  { path: 'mechanic/maintenance/edit/:recordId', component: MaintenanceFormComponent, canActivate: [authGuard], data: { roles: ADMIN_MANAGER_MECHANIC } },

  // Profile — all authenticated roles
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '' }
];

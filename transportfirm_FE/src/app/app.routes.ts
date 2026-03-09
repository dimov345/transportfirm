import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

const ADMIN_MANAGER = ['ADMIN', 'MANAGER'];
const ADMIN_MANAGER_DISPATCHER = ['ADMIN', 'MANAGER', 'DISPATCHER'];
const ADMIN_MANAGER_MECHANIC = ['ADMIN', 'MANAGER', 'MECHANIC'];
const ALL_STAFF = ['ADMIN', 'MANAGER', 'DISPATCHER', 'MECHANIC'];

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.Login)
  },
  {
    path: 'first-login',
    loadComponent: () => import('./pages/first-login/first-login').then(m => m.FirstLogin)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/forgot-password/forgot-password').then(m => m.ForgotPassword)
  },

  {
    path: '',
    loadComponent: () => import('./pages/home/home').then(m => m.Home),
    canActivate: [authGuard]
  },

  // Drivers — ADMIN, MANAGER, DISPATCHER
  {
    path: 'drivers',
    loadComponent: () => import('./components/drivers/drivers-list/drivers-list').then(m => m.DriversListComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },
  {
    path: 'drivers/new',
    loadComponent: () => import('./components/drivers/driver-form/driver-form').then(m => m.DriverFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'drivers/edit/:id',
    loadComponent: () => import('./components/drivers/driver-form/driver-form').then(m => m.DriverFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'drivers/:id',
    loadComponent: () => import('./components/drivers/driver-details/driver-details').then(m => m.DriverDetails),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },
  {
    path: 'driver-documents/:id',
    loadComponent: () => import('./components/drivers/driver-documents/driver-documents').then(m => m.DriverDocumentsComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },

  // Vehicles — all staff can view
  {
    path: 'vehicles',
    loadComponent: () => import('./components/vehicles/vehicle-list/vehicle-list').then(m => m.VehicleListComponent),
    canActivate: [authGuard],
    data: { roles: ALL_STAFF }
  },
  {
    path: 'vehicles/new',
    loadComponent: () => import('./components/vehicles/vehicle-form/vehicle-form').then(m => m.VehicleFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'vehicles/edit/:id',
    loadComponent: () => import('./components/vehicles/vehicle-form/vehicle-form').then(m => m.VehicleFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'vehicles/:id',
    loadComponent: () => import('./components/vehicles/vehicle-details/vehicle-details').then(m => m.VehicleDetails),
    canActivate: [authGuard],
    data: { roles: ALL_STAFF }
  },
  {
    path: 'vehicles-documents/:id',
    loadComponent: () => import('./components/vehicles/vehicle-documents/vehicle-documents').then(m => m.VehicleDocumentsComponent),
    canActivate: [authGuard],
    data: { roles: ALL_STAFF }
  },

  // Employees — ADMIN, MANAGER only
  {
    path: 'employees',
    loadComponent: () => import('./components/employees/employee-list/employee-list').then(m => m.EmployeeList),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'employees/new',
    loadComponent: () => import('./components/employees/employee-form/employee-form').then(m => m.EmployeeForm),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'employees/edit/:id',
    loadComponent: () => import('./components/employees/employee-form/employee-form').then(m => m.EmployeeForm),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'employees/details/:id',
    loadComponent: () => import('./components/employees/employee-details/employee-details').then(m => m.EmployeeDetails),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'employees-documents/:id',
    loadComponent: () => import('./components/employees/employee-documents/employee-documents').then(m => m.EmployeeDocuments),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },

  // Mechanics list/details — ADMIN, MANAGER only
  {
    path: 'mechanics',
    loadComponent: () => import('./components/mechanic/mechanic-list/mechanic-list').then(m => m.MechanicListComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'mechanics/:id',
    loadComponent: () => import('./components/mechanic/mechanic-details/mechanic-details').then(m => m.MechanicDetailsComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },

  // Freight Trips — ADMIN, MANAGER, DISPATCHER
  {
    path: 'freight-trips',
    loadComponent: () => import('./components/freight-trips/freight-trips-list/freight-trips-list').then(m => m.FreightTripsListComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },
  {
    path: 'freight-trips/new',
    loadComponent: () => import('./components/freight-trips/freight-trip-form/freight-trip-form').then(m => m.FreightTripFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },
  {
    path: 'freight-trips/edit/:id',
    loadComponent: () => import('./components/freight-trips/freight-trip-form/freight-trip-form').then(m => m.FreightTripFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },
  {
    path: 'freight-trips/:id',
    loadComponent: () => import('./components/freight-trips/freight-trip-details/freight-trip-details').then(m => m.FreightTripDetailsComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_DISPATCHER }
  },

  // Dispatchers list/details — ADMIN, MANAGER only
  {
    path: 'dispatchers',
    loadComponent: () => import('./components/dispatcher/dispatcher-list/dispatcher-list').then(m => m.DispatcherListComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },
  {
    path: 'dispatchers/:id',
    loadComponent: () => import('./components/dispatcher/dispatcher-details/dispatcher-details').then(m => m.DispatcherDetailsComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER }
  },

  // Dispatcher personal dashboard — DISPATCHER role
  {
    path: 'dispatcher',
    loadComponent: () => import('./components/dispatcher/dispatcher-dashboard/dispatcher-dashboard').then(m => m.DispatcherDashboardComponent),
    canActivate: [authGuard],
    data: { roles: [...ADMIN_MANAGER, 'DISPATCHER'] }
  },

  // Mechanic personal dashboard — MECHANIC role only
  {
    path: 'mechanic',
    loadComponent: () => import('./components/mechanic/mechanic-dashboard/mechanic-dashboard').then(m => m.MechanicDashboardComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_MECHANIC }
  },
  {
    path: 'mechanic/maintenance/new/:vehicleId',
    loadComponent: () => import('./components/mechanic/maintenance-form/maintenance-form').then(m => m.MaintenanceFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_MECHANIC }
  },
  {
    path: 'mechanic/maintenance/edit/:recordId',
    loadComponent: () => import('./components/mechanic/maintenance-form/maintenance-form').then(m => m.MaintenanceFormComponent),
    canActivate: [authGuard],
    data: { roles: ADMIN_MANAGER_MECHANIC }
  },

  // Accounting / Reports — ADMIN, MANAGER, ACCOUNTANT
  {
    path: 'reports',
    loadComponent: () => import('./components/accounting/accounting').then(m => m.AccountingComponent),
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'MANAGER', 'ACCOUNTANT'] }
  },

  // Profile — all authenticated roles
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },

  { path: '**', redirectTo: '' }
];

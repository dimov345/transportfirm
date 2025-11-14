import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { DriversListComponent } from './components/drivers/drivers-list/drivers-list';
import { DriverFormComponent } from './components/drivers/driver-form/driver-form';
import { DriverDetails } from './components/drivers/driver-details/driver-details';
import { DriverDocumentsComponent } from './components/drivers/driver-documents/driver-documents';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'drivers', component: DriversListComponent },
  { path: 'drivers/new', component: DriverFormComponent },
  { path: 'drivers/edit/:egn', component: DriverFormComponent },
  { path: 'drivers/:egn', component: DriverDetails },
  { path: 'driver-documents/:egn', component: DriverDocumentsComponent },
  { path: '**', redirectTo: '' }
];

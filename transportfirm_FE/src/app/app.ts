import { Component, inject } from '@angular/core';
import { SidebarComponent } from './shared/sidebar/sidebar';
import { ToastComponent } from './shared/toast/toast';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, ToastComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class AppComponent {
  isSidebarCollapsed = true;
  showSidebar = true;
  isAuthRoute = false;

  private router = inject(Router);

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        const url = e.urlAfterRedirects;

        this.isAuthRoute =
          url.startsWith('/login') ||
          url.startsWith('/first-login') ||
          url.startsWith('/forgot-password');

        this.showSidebar = !this.isAuthRoute;

        // по желание: нулираме sidebar state, когато сме на auth routes
        if (this.isAuthRoute) this.isSidebarCollapsed = false;
      });
  }

  onSidebarToggle(collapsed: boolean) {
    this.isSidebarCollapsed = collapsed;
  }
}

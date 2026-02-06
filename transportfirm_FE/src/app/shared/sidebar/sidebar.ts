import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { SIDEBAR_ITEMS } from './sidebar.config';

type IconName = 'home' | 'users' | 'folder' | 'truck' | 'lorry' | 'chart' | 'settings' | 'chevron';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss']
})
export class SidebarComponent {
  @Output() toggleWidth = new EventEmitter<boolean>();

  isExpanded = false;
  activeMenu: number | null = null;
  items = SIDEBAR_ITEMS;

  // ✅ SSR-safe default
  private isSmallScreen = false;

  constructor(private router: Router) {
    // ✅ SSR-safe check
    this.isSmallScreen =
      typeof window !== 'undefined' &&
      !!window.matchMedia &&
      window.matchMedia('(max-width: 768px)').matches;

    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => {
        this.activeMenu = null;

        if (this.isSmallScreen) {
          this.isExpanded = false;
          this.toggleWidth.emit(true);
        }
      });
  }

  onMouseEnter() {
    if (this.isSmallScreen) return;
    this.isExpanded = true;
    this.toggleWidth.emit(false);
  }

  onMouseLeave() {
    if (this.isSmallScreen) return;
    this.isExpanded = false;
    this.activeMenu = null;
    this.toggleWidth.emit(true);
  }

  toggleMenu(index: number) {
    if (!this.isExpanded && !this.isSmallScreen) {
      this.isExpanded = true;
      this.toggleWidth.emit(false);
    }
    this.activeMenu = this.activeMenu === index ? null : index;
  }

  iconName(icon: any): IconName {
    const s = String(icon || '').toLowerCase();
    const allowed = ['home', 'users', 'folder', 'truck', 'lorry', 'chart', 'settings', 'chevron'];
    return (allowed.includes(s) ? (s as IconName) : 'home');
  }

  trackByIndex = (i: number) => i;
}

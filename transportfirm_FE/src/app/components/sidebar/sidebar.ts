import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

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
  activeMenu: string | null = null;

  onMouseEnter() {
    this.isExpanded = true;
    this.toggleWidth.emit(false);
  }

  onMouseLeave() {
    this.isExpanded = false;
    this.activeMenu = null;
    this.toggleWidth.emit(true);
  }

  toggleMenu(menu: string) {
    this.activeMenu = this.activeMenu === menu ? null : menu;
  }
}
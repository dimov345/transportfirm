import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SIDEBAR_ITEMS } from './sidebar.config';

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

  onMouseEnter() {
    this.isExpanded = true;
    this.toggleWidth.emit(false);
  }

  onMouseLeave() {
    this.isExpanded = false;
    this.activeMenu = null;
    this.toggleWidth.emit(true);
  }

  toggleMenu(index: number) {
    this.activeMenu = this.activeMenu === index ? null : index;
  }
}

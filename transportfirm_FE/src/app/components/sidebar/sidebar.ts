import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  trigger,
  state,
  style,
  transition,
  animate,
} from '@angular/animations';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss'],
  animations: [
    // 📦 Ширина на целия sidebar
    trigger('expand', [
      state('collapsed', style({ width: '80px' })),
      state('expanded', style({ width: '260px' })),
      transition('collapsed <=> expanded', [
        animate('0.3s cubic-bezier(0.4, 0.0, 0.2, 1)')
      ])
    ]),

    // 🎨 Анимация на логото
    trigger('logoAnimation', [
      state('collapsed', style({ opacity: 0, transform: 'scale(0.8)' })),
      state('expanded', style({ opacity: 1, transform: 'scale(1)' })),
      transition('collapsed <=> expanded', animate('250ms ease-in-out')),
    ]),

    // 🧭 Основни елементи от менюто
    trigger('menuItem', [
      state('collapsed', style({ opacity: 0, transform: 'translateX(-10px)' })),
      state('expanded', style({ opacity: 1, transform: 'translateX(0)' })),
      transition('collapsed <=> expanded', animate('200ms ease-in-out')),
    ]),

    // 🔽 Завъртане на стрелката при подменю
    trigger('arrowRotate', [
      state('normal', style({ transform: 'rotate(0deg)' })),
      state('rotated', style({ transform: 'rotate(180deg)' })),
      transition('normal <=> rotated', animate('200ms ease-in-out')),
    ]),

    // 📂 Разгръщане на подменю
    trigger('submenu', [
      state('collapsed', style({ height: '0', opacity: 0, overflow: 'hidden' })),
      state('expanded', style({ height: '*', opacity: 1, overflow: 'hidden' })),
      transition('collapsed <=> expanded', animate('250ms ease-in-out')),
    ]),

    // 🪄 Елементи в подменюто
    trigger('submenuItem', [
      state('collapsed', style({ opacity: 0, transform: 'translateX(-10px)' })),
      state('expanded', style({ opacity: 1, transform: 'translateX(0)' })),
      transition('collapsed <=> expanded', animate('200ms ease-in-out')),
    ]),
  ]
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

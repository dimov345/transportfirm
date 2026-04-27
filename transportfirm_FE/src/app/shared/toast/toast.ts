import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Toast } from '../../core/services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (toast of notificationSvc.toasts(); track toast.id) {
        <div class="toast toast--{{ toast.type }}" role="alert" aria-live="assertive">
          <span class="toast__icon">{{ iconFor(toast.type) }}</span>
          <span class="toast__message">{{ toast.message }}</span>
          <button class="toast__close" (click)="notificationSvc.dismiss(toast.id)" aria-label="Затвори">✕</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 380px;
      pointer-events: none;
    }

    .toast {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 12px 16px;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.18);
      font-size: 0.875rem;
      line-height: 1.4;
      pointer-events: all;
      animation: toast-in 0.2s ease;
    }

    @keyframes toast-in {
      from { opacity: 0; transform: translateY(12px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .toast--error   { background: #fef2f2; border-left: 4px solid #ef4444; color: #7f1d1d; }
    .toast--warning { background: #fffbeb; border-left: 4px solid #f59e0b; color: #78350f; }
    .toast--success { background: #f0fdf4; border-left: 4px solid #22c55e; color: #14532d; }
    .toast--info    { background: #eff6ff; border-left: 4px solid #3b82f6; color: #1e3a5f; }

    .toast__icon { font-size: 1rem; flex-shrink: 0; margin-top: 1px; }
    .toast__message { flex: 1; }

    .toast__close {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 0.75rem;
      opacity: 0.6;
      padding: 0 2px;
      flex-shrink: 0;
      color: inherit;
      line-height: 1;
    }
    .toast__close:hover { opacity: 1; }
  `]
})
export class ToastComponent {
  readonly notificationSvc = inject(NotificationService);

  iconFor(type: Toast['type']): string {
    const icons: Record<Toast['type'], string> = {
      error:   '✖',
      warning: '⚠',
      success: '✔',
      info:    'ℹ'
    };
    return icons[type];
  }
}

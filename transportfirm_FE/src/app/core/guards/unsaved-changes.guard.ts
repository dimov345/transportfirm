import { CanDeactivateFn } from '@angular/router';

/**
 * Implement this interface in any form component that should warn
 * the user before navigating away with unsaved changes.
 */
export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

/**
 * Functional CanDeactivate guard.
 * Shows a browser confirm dialog if the component reports unsaved changes.
 * Compatible with Angular 20 standalone/zoneless setup.
 */
export const unsavedChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (component) => {
  if (component.hasUnsavedChanges()) {
    return window.confirm(
      'Имате незапазени промени. Сигурни ли сте, че искате да напуснете страницата?'
    );
  }
  return true;
};

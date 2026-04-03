import { Component, Input } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-empty-state',
  template: `
    <div class="empty-state">
      <div class="empty-state__icon">{{ icon }}</div>
      <p class="empty-state__msg">{{ message }}</p>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    .empty-state { text-align: center; padding: 4rem 1rem; }
    .empty-state__icon { font-size: 3rem; margin-bottom: 1rem; opacity: 0.5; }
    .empty-state__msg { font-size: 1.1rem; color: var(--ija-text-muted); margin: 0 0 1.25rem; }
  `],
})
export class EmptyStateComponent {
  @Input() message = 'Aucun element trouve';
  @Input() icon = '📭';
}

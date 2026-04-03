import { Component, Input } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-loading-spinner',
  template: `
    <div class="loading-spinner" [class.loading-spinner--inline]="inline">
      <div class="loading-spinner__circle"></div>
      <p *ngIf="message" class="loading-spinner__msg">{{ message }}</p>
    </div>
  `,
  styles: [`
    .loading-spinner { text-align: center; padding: 3rem 0; }
    .loading-spinner--inline { padding: 1rem 0; }
    .loading-spinner__circle {
      width: 40px; height: 40px;
      border: 3px solid var(--ija-border);
      border-top-color: var(--ija-primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 0.75rem;
    }
    .loading-spinner__msg { font-size: 0.9rem; color: var(--ija-text-muted); margin: 0; }
  `],
})
export class LoadingSpinnerComponent {
  @Input() message = '';
  @Input() inline = false;
}

import { animate, style, transition, trigger } from '@angular/animations';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Observable } from 'rxjs';
import { ToastItem, ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-stack',
  standalone: false,
  templateUrl: './toast-stack.component.html',
  styleUrl: './toast-stack.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('toastEnter', [
      transition(':enter', [
        style({ transform: 'translateX(110%)', opacity: 0 }),
        animate(
          '240ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ transform: 'translateX(0)', opacity: 1 }),
        ),
      ]),
      transition(':leave', [
        animate(
          '180ms ease',
          style({ transform: 'translateX(80px)', opacity: 0 }),
        ),
      ]),
    ]),
  ],
  host: { class: 'app-toast-host' },
})
export class ToastStackComponent {
  readonly toasts$: Observable<ToastItem[]>;

  constructor(private readonly toast: ToastService) {
    this.toasts$ = this.toast.toasts$;
  }

  trackById(_i: number, t: ToastItem): string {
    return t.id;
  }

  dismiss(id: string): void {
    this.toast.dismiss(id);
  }

  action(t: ToastItem): void {
    this.toast.runAction(t.id);
  }
}

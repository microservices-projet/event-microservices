import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastKind = 'info' | 'success' | 'warning' | 'danger';

export interface ToastItem {
  id: string;
  message: string;
  title?: string;
  kind: ToastKind;
  /** 0 = pas d’auto-fermeture */
  durationMs: number;
  actionLabel?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly items$ = new BehaviorSubject<ToastItem[]>([]);
  private readonly timers = new Map<string, ReturnType<typeof setTimeout>>();
  private readonly actionHandlers = new Map<string, () => void>();

  readonly toasts$ = this.items$.asObservable();

  show(options: {
    message: string;
    title?: string;
    kind?: ToastKind;
    durationMs?: number;
    actionLabel?: string;
    action?: () => void;
  }): string {
    const id = this.newId();
    const durationMs = options.durationMs ?? 5200;
    const item: ToastItem = {
      id,
      message: options.message,
      title: options.title,
      kind: options.kind ?? 'info',
      durationMs,
      actionLabel: options.actionLabel,
    };
    if (options.action) {
      this.actionHandlers.set(id, options.action);
    }
    this.items$.next([...this.items$.value, item]);
    if (durationMs > 0) {
      this.timers.set(
        id,
        setTimeout(() => this.dismiss(id), durationMs),
      );
    }
    return id;
  }

  success(message: string, title?: string): string {
    return this.show({ message, title, kind: 'success', durationMs: 3800 });
  }

  warning(message: string, title?: string): string {
    return this.show({ message, title, kind: 'warning', durationMs: 6000 });
  }

  danger(message: string, title?: string): string {
    return this.show({ message, title, kind: 'danger', durationMs: 8000 });
  }

  dismiss(id: string): void {
    const t = this.timers.get(id);
    if (t !== undefined) {
      clearTimeout(t);
      this.timers.delete(id);
    }
    this.actionHandlers.delete(id);
    this.items$.next(this.items$.value.filter((x) => x.id !== id));
  }

  runAction(id: string): void {
    const fn = this.actionHandlers.get(id);
    this.dismiss(id);
    fn?.();
  }

  private newId(): string {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;
  }
}

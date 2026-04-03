import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'statusBadge' })
export class StatusBadgePipe implements PipeTransform {
  private map: Record<string, string> = {
    PUBLISHED: 'ija-badge--success',
    DRAFT: 'ija-badge--warning',
    CANCELLED: 'ija-badge--danger',
    COMPLETED: 'ija-badge--muted',
    OPEN: 'ija-badge--info',
    IN_PROGRESS: 'ija-badge--warning',
    RESOLVED: 'ija-badge--success',
    CLOSED: 'ija-badge--muted',
    REJECTED: 'ija-badge--danger',
    CONFIRMED: 'ija-badge--success',
    PENDING: 'ija-badge--warning',
    EXPIRED: 'ija-badge--muted',
    APPROVED: 'ija-badge--success',
    FLAGGED: 'ija-badge--warning',
    DISPONIBLE: 'ija-badge--success',
    VENDU: 'ija-badge--info',
    ANNULE: 'ija-badge--danger',
    PAID: 'ija-badge--success',
    REFUNDED: 'ija-badge--info',
    FAILED: 'ija-badge--danger',
    ACTIVE: 'ija-badge--success',
    SUSPENDED: 'ija-badge--warning',
    DISABLED: 'ija-badge--danger',
    VIP: 'ija-badge--warning',
    INVITE: 'ija-badge--info',
    NORMAL: 'ija-badge--muted',
    CRITICAL: 'ija-badge--danger',
    HIGH: 'ija-badge--warning',
    MEDIUM: 'ija-badge--info',
    LOW: 'ija-badge--muted',
  };

  transform(value: string | null | undefined): string {
    if (!value) return 'ija-badge--muted';
    return this.map[value.toUpperCase()] || 'ija-badge--muted';
  }
}

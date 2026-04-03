import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'timeAgo' })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) return '';
    const date = new Date(value);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) return 'il y a quelques secondes';
    if (diffMin < 60) return `il y a ${diffMin} min`;
    if (diffHour < 24) return `il y a ${diffHour}h`;
    if (diffDay < 7) return `il y a ${diffDay}j`;
    if (diffDay < 30) return `il y a ${Math.floor(diffDay / 7)} sem`;
    if (diffDay < 365) return `il y a ${Math.floor(diffDay / 30)} mois`;
    return `il y a ${Math.floor(diffDay / 365)} an(s)`;
  }
}

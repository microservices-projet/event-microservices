import { Injectable } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { DataService } from './data.service';
import { Event } from '../../models/event';

export interface CalendarDay {
  ymd: string;
  dow: string;
  dayNumber: number;
  weekend: boolean;
  eventCount: number;
  dotCount: number;
  dots: number[];
}

@Injectable({ providedIn: 'root' })
export class CalendarStateService {
  private initialized = false;
  private initSub?: Subscription;

  private allEvents: Event[] = [];

  private loadingSubject = new BehaviorSubject<boolean>(true);
  loading$ = this.loadingSubject.asObservable();

  private monthCursor: Date = new Date();
  private monthLabelSubject = new BehaviorSubject<string>('');
  monthLabel$ = this.monthLabelSubject.asObservable();

  private selectedYmdSubject = new BehaviorSubject<string>('');
  selectedYmd$ = this.selectedYmdSubject.asObservable();

  private daysOfMonthSubject = new BehaviorSubject<CalendarDay[]>([]);
  daysOfMonth$ = this.daysOfMonthSubject.asObservable();

  private selectedDayEventsSubject = new BehaviorSubject<Event[]>([]);
  selectedDayEvents$ = this.selectedDayEventsSubject.asObservable();

  constructor(private dataService: DataService) {}

  ensureInitialized(): void {
    if (this.initialized) return;

    this.loadingSubject.next(true);
    this.initSub = this.dataService.getAll().subscribe({
      next: (events) => {
        this.allEvents = events ?? [];

        // UX: initialize strictly on today's real date.
        // (Even if there are no events today, the UI should show the actual current month/day.)
        this.monthCursor = new Date();
        this.selectedYmd = this.getLocalYmd(this.monthCursor);

        this.buildCalendarForMonth(this.monthCursor);
        this.recomputeSelectedDayEvents();

        this.initialized = true;
        this.loadingSubject.next(false);
      },
      error: () => {
        this.initialized = true;
        this.loadingSubject.next(false);
      },
    });
  }

  selectDay(ymd: string): void {
    this.selectedYmdSubject.next(ymd);
    this.recomputeSelectedDayEvents();
  }

  shiftMonth(delta: number): void {
    const y = this.monthCursor.getFullYear();
    const m = this.monthCursor.getMonth();
    this.monthCursor = new Date(y, m + delta, 1);

    this.buildCalendarForMonth(this.monthCursor);

    // If selected day is not in this month anymore, pick the first day that has an event.
    const selectedMonthPrefix = this.selectedYmdSubject.value?.slice(0, 7);
    const nextMonthPrefix = this.getMonthPrefixFromDate(this.monthCursor);
    if (selectedMonthPrefix !== nextMonthPrefix) {
      const eventYmds = this.allEvents
        .map((e) => this.parseEventYmd(e.date))
        .filter((v): v is string => !!v);

      const firstEventInMonth = Array.from(new Set(eventYmds))
        .filter((ymd) => ymd.slice(0, 7) === nextMonthPrefix)
        .sort()[0];

      this.selectedYmdSubject.next(firstEventInMonth ?? this.getLocalYmd(new Date(this.monthCursor.getFullYear(), this.monthCursor.getMonth(), 1)));
    }

    this.recomputeSelectedDayEvents();
  }

  goToToday(): void {
    this.monthCursor = new Date();
    this.selectedYmdSubject.next(this.getLocalYmd(this.monthCursor));
    this.buildCalendarForMonth(this.monthCursor);
    this.recomputeSelectedDayEvents();
  }

  private set selectedYmd(value: string) {
    this.selectedYmdSubject.next(value);
  }

  private recomputeSelectedDayEvents(): void {
    const selected = this.selectedYmdSubject.value;
    if (!selected) {
      this.selectedDayEventsSubject.next([]);
      return;
    }

    const filtered = this.allEvents.filter((e) => {
      const ymd = this.parseEventYmd(e.date);
      return ymd === selected;
    });

    this.selectedDayEventsSubject.next(filtered);
  }

  private buildCalendarForMonth(anchorDate: Date): void {
    const year = anchorDate.getFullYear();
    const monthIndex = anchorDate.getMonth();

    this.monthLabelSubject.next(
      new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(anchorDate),
    );

    const monthStart = new Date(year, monthIndex, 1);
    const monthEnd = new Date(year, monthIndex + 1, 0);

    // Precompute counts by local date (YYYY-MM-DD) to render dots.
    const countsByYmd = new Map<string, number>();
    for (const e of this.allEvents) {
      const ymd = this.parseEventYmd(e.date);
      if (!ymd) continue;
      countsByYmd.set(ymd, (countsByYmd.get(ymd) ?? 0) + 1);
    }

    const dowLabels = ['DIM', 'LUN', 'MAR', 'MER', 'JEU', 'VEN', 'SAM'];

    const days: CalendarDay[] = [];
    for (
      let d = new Date(monthStart);
      d.getTime() <= monthEnd.getTime();
      d = new Date(d.getTime() + 24 * 60 * 60 * 1000)
    ) {
      const dow = dowLabels[d.getDay()];
      const weekend = d.getDay() === 0 || d.getDay() === 6; // Sun or Sat
      const ymd = this.getLocalYmd(d);
      const eventCount = countsByYmd.get(ymd) ?? 0;
      const dotCount = Math.min(eventCount, 3);

      days.push({
        ymd,
        dow,
        dayNumber: d.getDate(),
        weekend,
        eventCount,
        dotCount,
        dots: Array(dotCount).fill(0),
      });
    }

    this.daysOfMonthSubject.next(days);
  }

  private getLocalYmd(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private parseEventYmd(dateStr: string): string | null {
    if (!dateStr) return null;

    // If backend sends ISO-like strings, use the date part to avoid timezone shifts.
    const match = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (match) return `${match[1]}-${match[2]}-${match[3]}`;

    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return null;
    return this.getLocalYmd(d);
  }

  private getMonthPrefixFromDate(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }

  private dateFromYmdPrefix(ymdPrefix: string): Date {
    // ymdPrefix = YYYY-MM
    const [yStr, mStr] = ymdPrefix.split('-');
    const year = Number(yStr);
    const monthIndex = Number(mStr) - 1;
    return new Date(year, monthIndex, 1);
  }
}


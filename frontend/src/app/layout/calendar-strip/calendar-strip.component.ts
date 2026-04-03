import { Component, OnDestroy, OnInit, Input } from '@angular/core';
import { Subscription } from 'rxjs';
import { CalendarStateService } from '../../shared/services/calendar-state.service';
import { CalendarDay } from '../../shared/services/calendar-state.service';

@Component({
  standalone: false,
  selector: 'app-calendar-strip',
  templateUrl: './calendar-strip.component.html',
  styleUrls: ['./calendar-strip.component.css'],
})
export class CalendarStripComponent implements OnInit, OnDestroy {
  @Input() compact = true;

  daysOfMonth: CalendarDay[] = [];
  displayDays: CalendarDay[] = [];
  selectedYmd = '';
  monthLabel = '';
  private subs: Subscription[] = [];

  constructor(private calendar: CalendarStateService) {}

  ngOnInit(): void {
    this.calendar.ensureInitialized();

    this.subs.push(
      this.calendar.daysOfMonth$.subscribe((d) => {
        this.daysOfMonth = d;
        this.recomputeDisplayDays();
      }),
      this.calendar.selectedYmd$.subscribe((y) => {
        this.selectedYmd = y;
        this.recomputeDisplayDays();
      }),
      this.calendar.monthLabel$.subscribe((m) => (this.monthLabel = m)),
    );
  }

  ngOnDestroy(): void {
    for (const s of this.subs) s.unsubscribe();
  }

  selectDay(ymd: string): void {
    this.calendar.selectDay(ymd);
  }

  shiftMonth(delta: number): void {
    this.calendar.shiftMonth(delta);
  }

  goToToday(): void {
    this.calendar.goToToday();
  }

  private recomputeDisplayDays(): void {
    if (!this.compact) {
      this.displayDays = this.daysOfMonth;
      return;
    }

    if (!this.daysOfMonth?.length || !this.selectedYmd) {
      this.displayDays = this.daysOfMonth;
      return;
    }

    const idx = this.daysOfMonth.findIndex((d) => d.ymd === this.selectedYmd);
    if (idx < 0) {
      this.displayDays = this.daysOfMonth;
      return;
    }

    // Show only a short, readable window around the selected day.
    const windowSize = 14;
    const half = Math.floor(windowSize / 2);
    let start = Math.max(0, idx - half);
    let end = Math.min(this.daysOfMonth.length, start + windowSize);
    start = Math.max(0, end - windowSize);

    this.displayDays = this.daysOfMonth.slice(start, end);
  }
}


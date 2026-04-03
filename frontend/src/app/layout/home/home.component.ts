import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Event } from '../../models/event';
import { CalendarStateService } from '../../shared/services/calendar-state.service';

@Component({
  standalone: false,
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit, OnDestroy {
  selectedDayEvents: Event[] = [];
  loading = true;
  private subs: Subscription[] = [];

  cities = [
    { name: 'TUNIS', image: 'https://images.unsplash.com/photo-1570843554605-8d2e414c49b0?w=600' },
    { name: 'HAMMAMET', image: 'https://images.unsplash.com/photo-1569949230765-2a8f4d8b3e3f?w=600' },
    { name: 'SOUSSE', image: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=600' },
    { name: 'SFAX', image: 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=600' },
    { name: 'DJERBA', image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600' },
    { name: 'LE KEF', image: 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=600' },
  ];

  categories = [
    { name: 'Concerts', icon: 'music', image: 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=600' },
    { name: 'Tech & IA', icon: 'cpu', image: 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=600' },
    { name: 'Sport', icon: 'activity', image: 'https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=600' },
    { name: 'Gastronomie', icon: 'coffee', image: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600' },
    { name: 'Art & Culture', icon: 'palette', image: 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=600' },
    { name: 'Bien-etre', icon: 'heart', image: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600' },
  ];

  constructor(private calendar: CalendarStateService) {}

  ngOnInit(): void {
    this.calendar.ensureInitialized();

    this.subs.push(
      this.calendar.selectedDayEvents$.subscribe((ev) => (this.selectedDayEvents = ev)),
      this.calendar.loading$.subscribe((l) => (this.loading = l)),
    );
  }

  ngOnDestroy(): void {
    for (const s of this.subs) s.unsubscribe();
    this.subs = [];
  }
}

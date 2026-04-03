import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Event } from '../../../models/event';
import { DataService } from '../../../shared/services/data.service';
import { AuthService } from '../../../core/services/auth.service';
import { RoleUiService } from '../../../core/services/role-ui.service';

@Component({
  standalone: false,
  selector: 'app-list-event',
  templateUrl: './list-event.component.html',
  styleUrls: ['./list-event.component.css'],
})
export class ListEventComponent implements OnInit {
  searchItem = '';
  eventList: Event[] = [];
  loading = true;
  /** From ?mine=1 — filter to current user's organized events */
  mineOnly = false;

  constructor(
    private dataService: DataService,
    private route: ActivatedRoute,
    public auth: AuthService,
    public roles: RoleUiService,
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((q) => {
      this.mineOnly = q.get('mine') === '1';
    });
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.dataService.getAll().subscribe({
      next: (events) => {
        this.eventList = events;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  get filteredEvents(): Event[] {
    let list = this.eventList;
    if (this.mineOnly) {
      const uid = this.auth.currentUser?.id;
      if (!uid || uid < 1) return [];
      list = list.filter((e) => e.organizerId === uid);
    }
    if (!this.searchItem.trim()) return list;
    const term = this.searchItem.toLowerCase().trim();
    return list.filter(
      (e) =>
        e.title.toLowerCase().includes(term) || e.place.toLowerCase().includes(term),
    );
  }

  onLike(event: Event): void {
    this.dataService.likeEvent(event.id).subscribe({
      next: (updated) => {
        const idx = this.eventList.findIndex((e) => e.id === updated.id);
        if (idx > -1) this.eventList[idx] = updated;
      },
    });
  }

  onBuy(event: Event): void {
    if (event.nbPlaces > 0) event.nbPlaces--;
  }
}

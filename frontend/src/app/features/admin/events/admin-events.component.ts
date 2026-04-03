import { Component, OnInit } from '@angular/core';
import { Event } from '../../../models/event';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';

@Component({
  standalone: false,
  selector: 'app-admin-events',
  templateUrl: './admin-events.component.html',
  styleUrls: ['./admin-events.component.css'],
})
export class AdminEventsComponent implements OnInit {
  events: Event[] = [];
  loading = true;
  keyword = '';
  status = '';
  archived = '';

  constructor(private read: AdminReadService, private write: AdminWriteService) {}

  ngOnInit(): void {
    this.reload();
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.keyword = '';
    this.status = '';
    this.archived = '';
    this.reload();
  }

  reload(): void {
    this.loading = true;
    const archived = this.archived === '' ? null : this.archived === 'true';
    this.read.getEvents({ keyword: this.keyword, status: this.status, archived }).subscribe({
      next: (data) => { this.events = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  archive(event: Event): void {
    this.write.deleteEvent(event.id).subscribe({ next: () => this.reload() });
  }
}

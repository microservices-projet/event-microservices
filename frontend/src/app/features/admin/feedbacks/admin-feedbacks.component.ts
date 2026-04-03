import { Component, OnInit } from '@angular/core';
import { Feedback } from '../../../models/feedback';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-admin-feedbacks',
  templateUrl: './admin-feedbacks.component.html',
  styleUrls: ['./admin-feedbacks.component.css'],
})
export class AdminFeedbacksComponent implements OnInit {
  feedbacks: Feedback[] = [];
  loading = true;
  userId = '';
  eventId = '';
  status = '';

  constructor(
    private read: AdminReadService,
    private write: AdminWriteService,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.userId = '';
    this.eventId = '';
    this.status = '';
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.read.getFeedbacks({
      userId: this.userId ? Number(this.userId) : null,
      eventId: this.eventId ? Number(this.eventId) : null,
      status: this.status,
    }).subscribe({
      next: (data) => { this.feedbacks = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  moderate(f: Feedback, status: Feedback['status']): void {
    const moderatedBy = this.auth.currentUser?.id ?? 0;
    this.write.moderateFeedback(f.id, status, moderatedBy, `Moderated as ${status}`).subscribe({ next: () => this.reload() });
  }
}

import { Component, OnInit } from '@angular/core';
import { FeedbackService } from '../../../shared/services/feedback.service';
import { Feedback } from '../../../models/feedback';

@Component({
  standalone: false,
  selector: 'app-feedback-list',
  templateUrl: './feedback-list.component.html',
  styleUrls: ['./feedback-list.component.css'],
})
export class FeedbackListComponent implements OnInit {
  feedbacks: Feedback[] = [];
  loading = true;

  constructor(private feedbackService: FeedbackService) {}

  ngOnInit(): void {
    this.feedbackService.getAll().subscribe({
      next: (data) => { this.feedbacks = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  statusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'ija-badge--success';
      case 'REJECTED': return 'ija-badge--danger';
      case 'FLAGGED': return 'ija-badge--warning';
      default: return 'ija-badge--muted';
    }
  }

  getStars(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i < rating ? 1 : 0);
  }
}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedbackService } from '../../../shared/services/feedback.service';
import { FeedbackStats } from '../../../models/feedback';

@Component({
  standalone: false,
  selector: 'app-feedback-stats',
  templateUrl: './feedback-stats.component.html',
  styleUrls: ['./feedback-stats.component.css'],
})
export class FeedbackStatsComponent implements OnInit {
  stats: FeedbackStats | null = null;
  loading = true;
  eventId!: number;

  constructor(private route: ActivatedRoute, private feedbackService: FeedbackService) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.params['eventId']);
    this.feedbackService.getStatsByEvent(this.eventId).subscribe({
      next: (data) => { this.stats = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  get ratingEntries(): [string, number][] {
    if (!this.stats?.ratingDistribution) return [];
    return Object.entries(this.stats.ratingDistribution).sort((a, b) => Number(b[0]) - Number(a[0]));
  }

  barWidth(count: number): number {
    if (!this.stats) return 0;
    return this.stats.totalFeedbacks > 0 ? (count / this.stats.totalFeedbacks) * 100 : 0;
  }
}

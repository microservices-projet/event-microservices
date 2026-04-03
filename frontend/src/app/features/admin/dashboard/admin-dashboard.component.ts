import { Component, OnInit } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';
import { AdminReadService } from '../services/admin-read.service';
import { AdminDashboardSummary } from '../../../models/admin-dashboard';
import { AnalyticsDashboard, AnalyticsService } from '../../../shared/services/analytics.service';

@Component({
  standalone: false,
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
})
export class AdminDashboardComponent implements OnInit {
  readonly kpiSkeletonSlots = [0, 1, 2, 3];
  loading = true;
  summary: AdminDashboardSummary | null = null;
  analyticsLoading = true;
  analytics: AnalyticsDashboard | null = null;
  analyticsError = false;

  constructor(
    private readonly adminRead: AdminReadService,
    private readonly analyticsApi: AnalyticsService,
    private readonly toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.reload();
    this.loadAnalytics();
  }

  reload(): void {
    this.loading = true;
    this.adminRead.getSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.toast.warning('Résumé admin indisponible (Config / Eureka / gateway).', 'Données agrégées');
      },
    });
  }

  loadAnalytics(): void {
    this.analyticsLoading = true;
    this.analyticsError = false;
    this.analyticsApi.getDashboard().subscribe({
      next: (data) => {
        this.analytics = data;
        this.analyticsLoading = false;
      },
      error: () => {
        this.analyticsError = true;
        this.analyticsLoading = false;
        this.toast.warning('Service analytics indisponible.', 'Kafka Streams');
      },
    });
  }

  refreshAll(): void {
    this.toast.show({
      title: 'Actualisation',
      message: 'Rechargement du résumé et des analytics…',
      kind: 'info',
      durationMs: 2600,
    });
    this.reload();
    this.loadAnalytics();
  }

  topTitles(): { title: string; count: number }[] {
    const raw = this.analytics?.topCreatedEventTitles ?? [];
    return raw.map((t) => ({
      title: t.title ?? '(sans titre)',
      count: t.count ?? 0,
    }));
  }

  objectEntries(obj: Record<string, number> | undefined): [string, number][] {
    if (!obj) return [];
    return Object.entries(obj).sort((a, b) => b[1] - a[1]).slice(0, 8);
  }
}

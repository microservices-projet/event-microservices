import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TopTitleCount {
  title?: string;
  count?: number;
}

export interface AnalyticsDashboard {
  reservationsByEventIdAggregated5mWindows?: Record<string, number>;
  reservationsByEventIdAggregated1hWindows?: Record<string, number>;
  reservationsPerMinuteSeriesLast60?: Record<string, number>;
  topCreatedEventTitles?: TopTitleCount[];
  ticketsSoldCounterRedis?: number;
  generatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private base = '/api/analytics';

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<AnalyticsDashboard> {
    return this.http.get<AnalyticsDashboard>(`${this.base}/dashboard`);
  }

  getReservationsPerMinute(minutes = 10): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.base}/reservations-per-minute`, {
      params: { minutes: String(minutes) },
    });
  }

  getTopEventTitles(limit = 5): Observable<TopTitleCount[]> {
    return this.http.get<TopTitleCount[]>(`${this.base}/top-event-titles`, {
      params: { limit: String(limit) },
    });
  }
}

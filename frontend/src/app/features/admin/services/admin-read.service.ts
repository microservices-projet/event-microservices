import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { UserResponse } from '../../../models/user';
import { Event } from '../../../models/event';
import { Reservation } from '../../../models/reservation';
import { Ticket } from '../../../models/ticket';
import { Feedback } from '../../../models/feedback';
import { Reclamation } from '../../../models/reclamation';
import { AdminDashboardSummary } from '../../../models/admin-dashboard';

@Injectable({ providedIn: 'root' })
export class AdminReadService {
  private base = '/api/users/admin';

  constructor(private http: HttpClient) {}

  getSummary(): Observable<AdminDashboardSummary> {
    return this.http.get<AdminDashboardSummary>(`${this.base}/dashboard/summary`).pipe(
      switchMap((summary) => {
        const hasData = (summary.usersCount + summary.eventsCount + summary.reservationsCount) > 0;
        if (hasData) return of(summary);
        return this.fallbackSummary();
      }),
      catchError(() => this.fallbackSummary()),
    );
  }

  getUsers(filters?: { query?: string; role?: string; status?: string }): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.base}/users`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<UserResponse[]>('/api/users').pipe(
          map((rows) => rows.filter((u) =>
            this.matchText(filters?.query, `${u.id} ${u.username} ${u.email}`) &&
            this.matchEq(filters?.role, u.role) &&
            this.matchEq(filters?.status, u.status),
          )),
        );
      }),
      catchError(() => this.http.get<UserResponse[]>('/api/users').pipe(
        map((rows) => rows.filter((u) =>
          this.matchText(filters?.query, `${u.id} ${u.username} ${u.email}`) &&
          this.matchEq(filters?.role, u.role) &&
          this.matchEq(filters?.status, u.status),
        )),
      )),
    );
  }

  getEvents(filters?: { keyword?: string; status?: string; archived?: boolean | null; organizerId?: number | null }): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.base}/events`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<Event[]>('/api/events').pipe(
          map((rows) => rows.filter((e) =>
            this.matchText(filters?.keyword, e.title) &&
            this.matchEq(filters?.status, e.status) &&
            (filters?.archived === null || filters?.archived === undefined || e.archived === filters.archived) &&
            (filters?.organizerId === null || filters?.organizerId === undefined || e.organizerId === filters.organizerId),
          )),
        );
      }),
      catchError(() => this.http.get<Event[]>('/api/events').pipe(
        map((rows) => rows.filter((e) =>
          this.matchText(filters?.keyword, e.title) &&
          this.matchEq(filters?.status, e.status) &&
          (filters?.archived === null || filters?.archived === undefined || e.archived === filters.archived) &&
          (filters?.organizerId === null || filters?.organizerId === undefined || e.organizerId === filters.organizerId),
        )),
      )),
    );
  }

  getReservations(filters?: {
    userId?: number | null;
    eventId?: number | null;
    from?: string;
    to?: string;
    status?: string;
    paymentStatus?: string;
  }): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.base}/reservations`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<Reservation[]>('/api/reservations').pipe(
          map((rows) => rows.filter((r) =>
            (filters?.userId === null || filters?.userId === undefined || r.userId === filters.userId) &&
            (filters?.eventId === null || filters?.eventId === undefined || r.eventId === filters.eventId) &&
            this.matchEq(filters?.status, r.status) &&
            this.matchEq(filters?.paymentStatus, r.paymentStatus) &&
            this.matchDateRange(r.reservationDate, filters?.from, filters?.to),
          )),
        );
      }),
      catchError(() => this.http.get<Reservation[]>('/api/reservations').pipe(
        map((rows) => rows.filter((r) =>
          (filters?.userId === null || filters?.userId === undefined || r.userId === filters.userId) &&
          (filters?.eventId === null || filters?.eventId === undefined || r.eventId === filters.eventId) &&
          this.matchEq(filters?.status, r.status) &&
          this.matchEq(filters?.paymentStatus, r.paymentStatus) &&
          this.matchDateRange(r.reservationDate, filters?.from, filters?.to),
        )),
      )),
    );
  }

  getTickets(filters?: { userId?: number | null; eventId?: number | null; statut?: string; typeTicket?: string }): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.base}/tickets`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<Ticket[]>('/api/tickets').pipe(
          map((rows) => rows.filter((t) =>
            (filters?.userId === null || filters?.userId === undefined || t.userId === filters.userId) &&
            (filters?.eventId === null || filters?.eventId === undefined || t.eventId === filters.eventId) &&
            this.matchEq(filters?.statut, t.statut) &&
            this.matchEq(filters?.typeTicket, t.typeTicket),
          )),
        );
      }),
      catchError(() => this.http.get<Ticket[]>('/api/tickets').pipe(
        map((rows) => rows.filter((t) =>
          (filters?.userId === null || filters?.userId === undefined || t.userId === filters.userId) &&
          (filters?.eventId === null || filters?.eventId === undefined || t.eventId === filters.eventId) &&
          this.matchEq(filters?.statut, t.statut) &&
          this.matchEq(filters?.typeTicket, t.typeTicket),
        )),
      )),
    );
  }

  getFeedbacks(filters?: { userId?: number | null; eventId?: number | null; status?: string }): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.base}/feedbacks`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<Feedback[]>('/api/feedbacks').pipe(
          map((rows) => rows.filter((f) =>
            (filters?.userId === null || filters?.userId === undefined || f.userId === filters.userId) &&
            (filters?.eventId === null || filters?.eventId === undefined || f.eventId === filters.eventId) &&
            this.matchEq(filters?.status, f.status),
          )),
        );
      }),
      catchError(() => this.http.get<Feedback[]>('/api/feedbacks').pipe(
        map((rows) => rows.filter((f) =>
          (filters?.userId === null || filters?.userId === undefined || f.userId === filters.userId) &&
          (filters?.eventId === null || filters?.eventId === undefined || f.eventId === filters.eventId) &&
          this.matchEq(filters?.status, f.status),
        )),
      )),
    );
  }

  getReclamations(filters?: { userId?: number | null; eventId?: number | null; status?: string; type?: string }): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.base}/reclamations`, { params: this.params(filters) }).pipe(
      switchMap((adminRows) => {
        if (adminRows.length > 0) return of(adminRows);
        return this.http.get<Reclamation[]>('/api/reclamations').pipe(
          map((rows) => rows.filter((r) =>
            (filters?.userId === null || filters?.userId === undefined || r.userId === filters.userId) &&
            (filters?.eventId === null || filters?.eventId === undefined || r.eventId === filters.eventId) &&
            this.matchEq(filters?.status, r.status) &&
            this.matchEq(filters?.type, r.type),
          )),
        );
      }),
      catchError(() => this.http.get<Reclamation[]>('/api/reclamations').pipe(
        map((rows) => rows.filter((r) =>
          (filters?.userId === null || filters?.userId === undefined || r.userId === filters.userId) &&
          (filters?.eventId === null || filters?.eventId === undefined || r.eventId === filters.eventId) &&
          this.matchEq(filters?.status, r.status) &&
          this.matchEq(filters?.type, r.type),
        )),
      )),
    );
  }

  private fallbackSummary(): Observable<AdminDashboardSummary> {
    return this.http.get<UserResponse[]>('/api/users').pipe(
      switchMap((users) => this.http.get<Event[]>('/api/events').pipe(
        switchMap((events) => this.http.get<Reservation[]>('/api/reservations').pipe(
          switchMap((reservations) => this.http.get<Ticket[]>('/api/tickets').pipe(
            switchMap((tickets) => this.http.get<Feedback[]>('/api/feedbacks').pipe(
              switchMap((feedbacks) => this.http.get<Reclamation[]>('/api/reclamations').pipe(
                map((reclamations) => {
                  const paid = reservations.filter((r) => r.paymentStatus === 'PAID');
                  const revenue = paid.reduce((sum, r) => sum + (r.totalPrice || 0), 0);
                  return {
                    usersCount: users.length,
                    eventsCount: events.length,
                    reservationsCount: reservations.length,
                    revenue,
                    paidReservationsCount: paid.length,
                    reservationsByStatus: this.countBy(reservations.map((r) => r.status)),
                    ticketsByStatus: this.countBy(tickets.map((t) => t.statut)),
                    feedbacksByStatus: this.countBy(feedbacks.map((f) => f.status)),
                    reclamationsByStatus: this.countBy(reclamations.map((r) => r.status)),
                  } as AdminDashboardSummary;
                }),
              )),
            )),
          )),
        )),
      )),
    );
  }

  private countBy(values: Array<string | undefined>): Record<string, number> {
    return values.reduce((acc, v) => {
      const key = (v || 'UNKNOWN').toString();
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
  }

  private matchEq(expected: unknown, actual: unknown): boolean {
    if (expected === null || expected === undefined || `${expected}`.trim() === '') return true;
    return `${expected}`.toUpperCase() === `${actual ?? ''}`.toUpperCase();
  }

  private matchText(search: unknown, source: unknown): boolean {
    if (search === null || search === undefined || `${search}`.trim() === '') return true;
    return `${source ?? ''}`.toLowerCase().includes(`${search}`.trim().toLowerCase());
  }

  private matchDateRange(raw: string | undefined, from?: string, to?: string): boolean {
    if (!from && !to) return true;
    if (!raw) return false;
    const d = new Date(raw);
    if (Number.isNaN(d.getTime())) return false;
    if (from) {
      const fromDate = new Date(from);
      if (!Number.isNaN(fromDate.getTime()) && d < fromDate) return false;
    }
    if (to) {
      const toDate = new Date(to);
      if (!Number.isNaN(toDate.getTime()) && d > toDate) return false;
    }
    return true;
  }

  private params(filters: Record<string, unknown> = {}): HttpParams {
    let params = new HttpParams();
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== null && v !== undefined && `${v}`.trim() !== '') {
        params = params.set(k, `${v}`);
      }
    });
    return params;
  }
}

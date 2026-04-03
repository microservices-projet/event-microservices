import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Reservation,
  ReservationRequest,
  CancelRequest,
  AvailabilityResponse,
} from '../../models/reservation';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private apiUrl = '/api/reservations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  getById(id: string): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.apiUrl}/${id}`);
  }

  getByUser(userId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/user/${userId}`);
  }

  getByEvent(eventId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/event/${eventId}`);
  }

  create(request: ReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(this.apiUrl, request);
  }

  checkAvailability(eventId: number): Observable<AvailabilityResponse> {
    return this.http.get<AvailabilityResponse>(`${this.apiUrl}/availability/${eventId}`);
  }

  confirm(id: string): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.apiUrl}/${id}/confirm`, {});
  }

  cancel(id: string, reason: string): Observable<Reservation> {
    const body: CancelRequest = { reason, cancelledBy: 0 };
    return this.http.patch<Reservation>(`${this.apiUrl}/${id}/cancel`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

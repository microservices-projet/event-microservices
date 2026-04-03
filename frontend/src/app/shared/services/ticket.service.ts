import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../../models/ticket';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private apiUrl = '/api/tickets';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.apiUrl);
  }

  getById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`);
  }

  getByEvent(eventId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/event/${eventId}`);
  }

  getByUser(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/user/${userId}`);
  }

  create(eventId: number, ticket: Partial<Ticket>): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/event/${eventId}`, ticket);
  }

  update(id: number, ticket: Partial<Ticket>): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}`, ticket);
  }

  updateStatus(id: number, statut: string): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}/statut`, null, { params: { statut } });
  }

  updateType(id: number, typeTicket: string): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}/type`, null, { params: { typeTicket } });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

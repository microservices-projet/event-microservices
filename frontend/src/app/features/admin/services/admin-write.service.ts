import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DataService } from '../../../shared/services/data.service';
import { ReservationService } from '../../../shared/services/reservation.service';
import { TicketService } from '../../../shared/services/ticket.service';
import { FeedbackService } from '../../../shared/services/feedback.service';
import { ReclamationService } from '../../../shared/services/reclamation.service';
import { Event, EventRequest } from '../../../models/event';
import { Reservation } from '../../../models/reservation';
import { Ticket } from '../../../models/ticket';
import { Feedback } from '../../../models/feedback';
import { Reclamation } from '../../../models/reclamation';
import { UserResponse } from '../../../models/user';

@Injectable({ providedIn: 'root' })
export class AdminWriteService {
  constructor(
    private http: HttpClient,
    private events: DataService,
    private reservations: ReservationService,
    private tickets: TicketService,
    private feedbacks: FeedbackService,
    private reclamations: ReclamationService,
  ) {}

  updateUserRole(id: number, role: 'USER' | 'ADMIN' | 'MODERATOR'): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`/api/users/${id}/role`, { role });
  }

  updateUserStatus(id: number, status: 'ACTIVE' | 'SUSPENDED' | 'DISABLED'): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`/api/users/${id}/status`, { status });
  }

  createEvent(request: EventRequest): Observable<Event> {
    return this.events.createEvent(request);
  }

  updateEvent(id: number, request: EventRequest): Observable<Event> {
    return this.events.updateEvent(id, request);
  }

  deleteEvent(id: number): Observable<void> {
    return this.events.deleteEvent(id);
  }

  cancelReservation(id: string, reason: string, cancelledBy: number): Observable<Reservation> {
    return this.http.patch<Reservation>(`/api/reservations/${id}/cancel`, { reason, cancelledBy });
  }

  updateTicketStatus(id: number, statut: 'DISPONIBLE' | 'VENDU' | 'ANNULE'): Observable<Ticket> {
    return this.tickets.updateStatus(id, statut);
  }

  updateTicketType(id: number, typeTicket: 'NORMAL' | 'VIP' | 'INVITE'): Observable<Ticket> {
    return this.tickets.updateType(id, typeTicket);
  }

  deleteTicket(id: number): Observable<void> {
    return this.tickets.delete(id);
  }

  moderateFeedback(
    id: number,
    status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'FLAGGED',
    moderatedBy: number,
    moderationNote: string,
  ): Observable<Feedback> {
    return this.http.patch<Feedback>(`/api/feedbacks/${id}/moderate`, { status, moderatedBy, moderationNote });
  }

  assignReclamation(id: number, assignedTo: number): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`/api/reclamations/${id}/assign`, { assignedTo });
  }

  respondReclamation(id: number, response: string): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`/api/reclamations/${id}/respond`, { response });
  }

  updateReclamationStatus(id: number, status: Reclamation['status']): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`/api/reclamations/${id}/status`, { status });
  }
}

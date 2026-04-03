import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reclamation, ReclamationRequest } from '../../models/reclamation';

@Injectable({ providedIn: 'root' })
export class ReclamationService {
  private apiUrl = '/api/reclamations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(this.apiUrl);
  }

  getById(id: number): Observable<Reclamation> {
    return this.http.get<Reclamation>(`${this.apiUrl}/${id}`);
  }

  getByUser(userId: number): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.apiUrl}/user/${userId}`);
  }

  getByEvent(eventId: number): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.apiUrl}/event/${eventId}`);
  }

  getByStatus(status: string): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.apiUrl}/status/${status}`);
  }

  create(request: ReclamationRequest): Observable<Reclamation> {
    const body: Record<string, unknown> = {
      userId: request.userId,
      subject: request.subject,
      description: request.description,
    };
    if (request.type) body['type'] = request.type;
    if (request.eventId != null) body['eventId'] = request.eventId;
    if (request.ticketId != null) body['ticketId'] = request.ticketId;
    if (request.reservationId) body['reservationId'] = request.reservationId;
    return this.http.post<Reclamation>(this.apiUrl, body);
  }

  assign(id: number, assignedTo: number): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/assign`, { assignedTo });
  }

  respond(id: number, response: string): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/respond`, { response });
  }

  updateStatus(id: number, status: string): Observable<Reclamation> {
    return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
}

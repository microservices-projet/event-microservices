import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, EventRequest } from '../../models/event';

@Injectable({ providedIn: 'root' })
export class DataService {
  private apiUrl = '/api/events';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  getById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  createEvent(request: EventRequest): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, request);
  }

  updateEvent(id: number, request: EventRequest): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/${id}`, request);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  likeEvent(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/${id}/like`, {});
  }

  searchEvents(keyword: string): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/search`, { params: { keyword } });
  }

  getByOrganizer(organizerId: number): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/organizer/${organizerId}`);
  }
}

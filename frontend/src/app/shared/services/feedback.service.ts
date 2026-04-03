import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Feedback, FeedbackRequest, FeedbackStats } from '../../models/feedback';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private apiUrl = '/api/feedbacks';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(this.apiUrl);
  }

  getById(id: number): Observable<Feedback> {
    return this.http.get<Feedback>(`${this.apiUrl}/${id}`);
  }

  getByEvent(eventId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.apiUrl}/event/${eventId}`);
  }

  getByUser(userId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.apiUrl}/user/${userId}`);
  }

  getStatsByEvent(eventId: number): Observable<FeedbackStats> {
    return this.http.get<FeedbackStats>(`${this.apiUrl}/stats/event/${eventId}`);
  }

  create(request: FeedbackRequest): Observable<Feedback> {
    return this.http.post<Feedback>(this.apiUrl, request);
  }

  moderate(id: number, body: { status: string; moderatedBy: number; moderationNote?: string }): Observable<Feedback> {
    return this.http.patch<Feedback>(`${this.apiUrl}/${id}/moderate`, body);
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
}

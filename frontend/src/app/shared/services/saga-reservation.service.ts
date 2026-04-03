import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SagaReserveRequest, SagaReserveResponse } from '../../models/reservation';

@Injectable({ providedIn: 'root' })
export class SagaReservationService {
  private readonly baseUrl = '/api/saga';

  constructor(private http: HttpClient) {}

  reserve(body: SagaReserveRequest): Observable<SagaReserveResponse> {
    return this.http.post<SagaReserveResponse>(`${this.baseUrl}/reserve`, body);
  }
}

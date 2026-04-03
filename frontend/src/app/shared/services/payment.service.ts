import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentCallbackRequest, PaymentSession } from '../../models/payment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private apiUrl = '/api/payments';

  constructor(private http: HttpClient) {}

  getByReservationId(reservationId: string): Observable<PaymentSession> {
    return this.http.get<PaymentSession>(`${this.apiUrl}/reservation/${reservationId}`);
  }

  callback(paymentId: string, request: PaymentCallbackRequest): Observable<PaymentSession> {
    return this.http.post<PaymentSession>(`${this.apiUrl}/${paymentId}/callback`, request);
  }

  retry(paymentId: string): Observable<PaymentSession> {
    return this.http.post<PaymentSession>(`${this.apiUrl}/${paymentId}/retry`, {});
  }
}

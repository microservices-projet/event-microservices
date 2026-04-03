export type PaymentSessionStatus = 'PENDING' | 'INITIATED' | 'COMPLETED' | 'FAILED';

export interface PaymentSession {
  paymentId: string;
  reservationId: string;
  status: PaymentSessionStatus;
  sessionId: string;
  amount?: number;
  currency?: string;
  provider?: string;
}

export interface PaymentCallbackRequest {
  success: boolean;
  failureReason?: string;
}

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'FAILED';
export type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED' | 'FAILED';

export interface AuditEntry {
  action: string;
  performedBy: number;
  timestamp: string;
  details: string;
}

export interface Reservation {
  id: string;
  userId: number;
  eventId: number;
  ticketId?: number | null;
  numberOfPlaces: number;
  totalPrice: number;
  status: ReservationStatus;
  paymentStatus: PaymentStatus;
  reservationDate: string;
  confirmationDate: string;
  cancellationDate: string;
  cancellationReason: string;
  auditLog: AuditEntry[];
  createdAt: string;
  updatedAt: string;
  username: string;
  eventTitle: string;
  eventDate: string;
}

export interface ReservationRequest {
  userId: number;
  eventId: number;
  numberOfPlaces: number;
}

export interface CancelRequest {
  reason: string;
  cancelledBy: number;
}

export interface AvailabilityResponse {
  eventId: number;
  totalPlaces: number;
  reservedPlaces: number;
  availablePlaces: number;
}

/** POST /api/saga/reserve — orchestrated distributed reservation */
export interface SagaReserveRequest {
  userId: number;
  eventId: number;
  places: number;
  idempotencyKey?: string;
  nomClient?: string;
  emailClient?: string;
}

export interface SagaReserveResponse {
  sagaId: string;
  status: string;
  reservationId?: string;
  ticketId?: number;
  message?: string;
}

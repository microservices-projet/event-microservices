export interface Reclamation {
  id: number;
  userId: number;
  eventId?: number;
  reservationId?: string;
  ticketId?: number;
  subject: string;
  description: string;
  type: 'EVENT_ISSUE' | 'RESERVATION_ISSUE' | 'TICKET_ISSUE' | 'PAYMENT_ISSUE' | 'OTHER';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REJECTED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  assignedTo?: number;
  response?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt?: string;
  username?: string;
  eventTitle?: string;
}

export interface ReclamationRequest {
  userId: number;
  eventId?: number;
  reservationId?: string;
  ticketId?: number;
  subject: string;
  description: string;
  type?: string;
}

export interface Ticket {
  idTicket: number;
  eventId: number;
  userId: number;
  nomClient: string;
  emailClient: string;
  prix: number;
  eventTitle: string;
  statut: 'DISPONIBLE' | 'VENDU' | 'ANNULE';
  typeTicket: 'NORMAL' | 'VIP' | 'INVITE';
  nombreMaxTickets: number;
  dateCreation: string;
}

import { Component, OnInit } from '@angular/core';
import { Ticket } from '../../../models/ticket';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';

@Component({
  standalone: false,
  selector: 'app-admin-tickets',
  templateUrl: './admin-tickets.component.html',
  styleUrls: ['./admin-tickets.component.css'],
})
export class AdminTicketsComponent implements OnInit {
  tickets: Ticket[] = [];
  loading = true;
  eventId = '';
  userId = '';
  statut = '';
  typeTicket = '';

  constructor(private read: AdminReadService, private write: AdminWriteService) {}

  ngOnInit(): void {
    this.reload();
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.eventId = '';
    this.userId = '';
    this.statut = '';
    this.typeTicket = '';
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.read.getTickets({
      eventId: this.eventId ? Number(this.eventId) : null,
      userId: this.userId ? Number(this.userId) : null,
      statut: this.statut,
      typeTicket: this.typeTicket,
    }).subscribe({
      next: (data) => { this.tickets = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  setStatus(t: Ticket, statut: 'DISPONIBLE' | 'VENDU' | 'ANNULE'): void {
    this.write.updateTicketStatus(t.idTicket, statut).subscribe({ next: () => this.reload() });
  }
}

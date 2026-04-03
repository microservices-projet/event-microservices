import { Component, OnInit } from '@angular/core';
import { TicketService } from '../../shared/services/ticket.service';
import { Ticket } from '../../models/ticket';
import { AuthService } from '../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-tickets',
  templateUrl: './tickets.component.html',
  styleUrls: ['./tickets.component.css'],
})
export class TicketsComponent implements OnInit {
  tickets: Ticket[] = [];
  loading = true;
  filterStatus = '';

  constructor(
    private ticketService: TicketService,
    private auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.currentUser?.role === 'ADMIN';
  }

  ngOnInit(): void {
    if (!this.isAdmin) {
      this.loading = false;
      this.tickets = [];
      return;
    }

    this.ticketService.getAll().subscribe({
      next: (data) => { this.tickets = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  get filteredTickets(): Ticket[] {
    if (!this.filterStatus) return this.tickets;
    return this.tickets.filter(t => t.statut === this.filterStatus);
  }

  statusClass(statut: string): string {
    switch (statut) {
      case 'DISPONIBLE': return 'ija-badge--success';
      case 'VENDU': return 'ija-badge--info';
      case 'ANNULE': return 'ija-badge--danger';
      default: return 'ija-badge--muted';
    }
  }

  typeClass(type: string): string {
    switch (type) {
      case 'VIP': return 'ija-badge--warning';
      case 'INVITE': return 'ija-badge--info';
      default: return 'ija-badge--muted';
    }
  }
}

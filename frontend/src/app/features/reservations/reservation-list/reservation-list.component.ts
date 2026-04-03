import { Component, OnInit } from '@angular/core';
import { ReservationService } from '../../../shared/services/reservation.service';
import { Reservation } from '../../../models/reservation';
import { AuthService } from '../../../core/services/auth.service';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Component({
  standalone: false,
  selector: 'app-reservation-list',
  templateUrl: './reservation-list.component.html',
  styleUrls: ['./reservation-list.component.css'],
})
export class ReservationListComponent implements OnInit {
  reservations: Reservation[] = [];
  loading = true;
  filterStatus = '';
  private profileHydrateTried = false;

  constructor(
    private reservationService: ReservationService,
    private auth: AuthService,
    private keycloak: KeycloakService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.currentUser?.role === 'ADMIN';
  }

  ngOnInit(): void {
    this.loadReservations();
  }

  private loadReservations(): void {
    if (this.isAdmin) {
      this.reservationService.getAll().subscribe({
        next: (data) => { this.reservations = data; this.loading = false; },
        error: () => this.loading = false,
      });
      return;
    }

    const userId = this.auth.currentUser?.id;
    if (
      this.keycloak.authenticated &&
      (userId === undefined || userId === null || userId === 0) &&
      !this.profileHydrateTried
    ) {
      this.profileHydrateTried = true;
      void this.auth.hydrateFromKeycloak().then(() => this.loadReservations());
      return;
    }

    if (userId === undefined || userId === null || userId === 0) {
      this.reservations = [];
      this.loading = false;
      return;
    }

    this.reservationService.getByUser(userId).subscribe({
      next: (data) => { this.reservations = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  get filteredReservations(): Reservation[] {
    if (!this.filterStatus) return this.reservations;
    return this.reservations.filter(r => r.status === this.filterStatus);
  }

  statusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED': return 'ija-badge--success';
      case 'PENDING': return 'ija-badge--warning';
      case 'CANCELLED': return 'ija-badge--danger';
      case 'EXPIRED': return 'ija-badge--muted';
      case 'FAILED': return 'ija-badge--danger';
      default: return 'ija-badge--muted';
    }
  }

  paymentClass(status: string): string {
    switch (status) {
      case 'PAID': return 'ija-badge--success';
      case 'PENDING': return 'ija-badge--warning';
      case 'REFUNDED': return 'ija-badge--info';
      case 'FAILED': return 'ija-badge--danger';
      default: return 'ija-badge--muted';
    }
  }
}

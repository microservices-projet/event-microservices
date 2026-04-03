import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ReservationService } from '../../../shared/services/reservation.service';

@Component({
  standalone: false,
  selector: 'app-ticket-purchase-card',
  templateUrl: './ticket-purchase-card.component.html',
  styleUrls: ['./ticket-purchase-card.component.css'],
})
export class TicketPurchaseCardComponent implements OnInit, OnChanges {
  @Input() eventId!: number;
  @Input() unitPrice!: number;
  @Input() eventTitle?: string;

  nbPlace = 1;
  totalPrice = 0;

  loading = false;
  error = '';
  success = '';

  availablePlaces: number | null = null;
  availabilityLoading = false;

  constructor(
    private reservationService: ReservationService,
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.recalcTotal();
    if (this.eventId) {
      this.refreshAvailability();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['unitPrice'] || changes['eventId']) {
      this.recalcTotal();
      if (this.eventId) this.refreshAvailability();
    }
  }

  recalcTotal(): void {
    const price = Number(this.unitPrice ?? 0);
    this.totalPrice = Math.max(0, this.nbPlace) * price;
  }

  private refreshAvailability(): void {
    this.availabilityLoading = true;
    this.reservationService.checkAvailability(this.eventId).subscribe({
      next: (resp) => {
        this.availablePlaces = resp.availablePlaces;
        this.availabilityLoading = false;
      },
      error: () => {
        // Availability is a UX hint; don't block purchase on errors.
        this.availablePlaces = null;
        this.availabilityLoading = false;
      },
    });
  }

  onSubmit(): void {
    this.error = '';
    this.success = '';

    if (!this.eventId) {
      this.error = 'Evenement introuvable.';
      return;
    }

    if (this.loading) return;
    if (!this.auth.currentUser) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (this.nbPlace < 1) {
      this.error = 'Veuillez sélectionner au moins 1 place.';
      return;
    }

    // Optional guard: if availability is known and user exceeds it, block early.
    if (this.availablePlaces != null && this.nbPlace > this.availablePlaces) {
      this.error = `Places insuffisantes. Disponible: ${this.availablePlaces}`;
      return;
    }

    this.loading = true;

    const payload = {
      userId: this.auth.currentUser.id,
      eventId: this.eventId,
      numberOfPlaces: this.nbPlace,
    };

    this.reservationService.create(payload).subscribe({
      next: (created) => {
        this.loading = false;
        this.success = 'Reservation en attente de paiement.';
        this.router.navigate(['/reservations', created.id]);
      },
      error: (err) => {
        this.loading = false;
        const body = err?.error;
        const msg =
          (typeof body === 'object' && body?.message) ||
          (typeof body === 'string' ? body : null) ||
          err?.message;
        this.error =
          (typeof msg === 'string' && msg) ||
          (err?.status === 401
            ? 'Session expiree ou non connecte. Reconnectez-vous puis reessayez.'
            : 'Erreur lors de la reservation.');
      },
    });
  }
}


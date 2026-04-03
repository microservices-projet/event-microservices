import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ReservationService } from '../../../shared/services/reservation.service';
import { SagaReservationService } from '../../../shared/services/saga-reservation.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { AvailabilityResponse, SagaReserveResponse } from '../../../models/reservation';
import { HttpErrorResponse } from '@angular/common/http';

function parseSagaHttpError(err: HttpErrorResponse): string {
  const b = err.error;
  if (b && typeof b === 'object' && 'message' in b && typeof (b as { message: unknown }).message === 'string') {
    return (b as { message: string }).message;
  }
  if (typeof b === 'string' && b.trim().length > 0) {
    return b.trim();
  }
  return err.message || 'Erreur réseau ou serveur.';
}

@Component({
  standalone: false,
  selector: 'app-reservation-create',
  templateUrl: './reservation-create.component.html',
  styleUrls: ['./reservation-create.component.css'],
})
export class ReservationCreateComponent {
  form: FormGroup;
  loading = false;
  error = '';
  availability: AvailabilityResponse | null = null;
  checkingAvailability = false;

  get isAdmin(): boolean {
    return this.auth.currentUser?.role === 'ADMIN';
  }

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    private sagaReservation: SagaReservationService,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService,
  ) {
    this.form = this.fb.group({
      eventId: ['', [Validators.required, Validators.min(1)]],
      numberOfPlaces: [1, [Validators.required, Validators.min(1)]],
    });
  }

  checkAvailability(): void {
    const eventId = this.form.get('eventId')?.value;
    if (!eventId) return;
    this.checkingAvailability = true;
    this.reservationService.checkAvailability(Number(eventId)).subscribe({
      next: (data) => {
        this.availability = data;
        this.checkingAvailability = false;
      },
      error: () => {
        this.availability = null;
        this.checkingAvailability = false;
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;
    const places = Number(this.form.value.numberOfPlaces);
    const eventId = Number(this.form.value.eventId);
    if (this.availability && places > this.availability.availablePlaces) {
      this.error = `Places insuffisantes : ${this.availability.availablePlaces} disponible(s).`;
      this.toast.warning(this.error, 'Disponibilité');
      return;
    }

    this.loading = true;
    this.error = '';
    const user = this.auth.currentUser;
    const userId = user?.id ?? 0;
    const idempotencyKey =
      typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(36).slice(2, 12)}`;

    this.sagaReservation
      .reserve({
        userId,
        eventId,
        places,
        idempotencyKey,
        nomClient: user?.username,
        emailClient: user?.email,
      })
      .subscribe({
        next: (res: SagaReserveResponse) => {
          this.loading = false;
          if (res.status === 'COMPLETED' && res.reservationId) {
            this.toast.success(
              `Réservation et billet créés (saga). Billet #${res.ticketId ?? '—'}.`,
              'Orchestration',
            );
            this.router.navigate(['/reservations', res.reservationId]);
            return;
          }
          this.error = res.message || 'Le saga n’a pas pu terminer la réservation.';
          this.toast.warning(this.error, 'Saga');
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          const msg = parseSagaHttpError(err);
          this.error = msg;
          const title = err.status === 409 ? 'Saga — échec métier' : err.status === 400 ? 'Requête invalide' : 'Saga';
          this.toast.danger(msg, title);
        },
      });
  }
}

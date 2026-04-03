import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationService } from '../../../shared/services/reservation.service';
import { Reservation } from '../../../models/reservation';
import { RoleUiService } from '../../../core/services/role-ui.service';
import { PaymentService } from '../../../shared/services/payment.service';
import { PaymentSession } from '../../../models/payment';
import { ToastService } from '../../../core/services/toast.service';
import { catchError, forkJoin, interval, of, Subscription } from 'rxjs';

@Component({
  standalone: false,
  selector: 'app-reservation-detail',
  templateUrl: './reservation-detail.component.html',
  styleUrls: ['./reservation-detail.component.css'],
})
export class ReservationDetailComponent implements OnInit, OnDestroy {
  reservation: Reservation | null = null;
  paymentSession: PaymentSession | null = null;
  loading = true;
  cancelReason = '';
  showCancelPanel = false;
  actionError = '';
  actionLoading = false;
  /** User must acknowledge payment details before finalize / retry / simulate failure */
  paymentVerifyAcknowledged = false;

  private pollSub: Subscription | null = null;
  private reservationId = '';
  private toastBaselineSet = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reservationService: ReservationService,
    private paymentService: PaymentService,
    public roles: RoleUiService,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.reservationId = this.route.snapshot.params['id'];
    this.reloadReservation();
    this.pollSub = interval(3000).subscribe(() => this.refreshAsyncState());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  private reloadReservation(): void {
    this.loading = true;
    forkJoin({
      reservation: this.reservationService.getById(this.reservationId),
      payment: this.paymentService.getByReservationId(this.reservationId).pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ reservation, payment }) => {
        const prevSession = this.paymentSession;
        this.reservation = reservation;
        this.paymentSession = payment;
        this.loading = false;
        this.toastBaselineSet = true;
        this.syncPaymentVerifyAck(prevSession, payment);
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/reservations']);
      },
    });
  }

  private refreshAsyncState(): void {
    const prevRes = this.reservation;
    const prevSession = this.paymentSession;
    forkJoin({
      reservation: this.reservationService.getById(this.reservationId),
      payment: this.paymentService.getByReservationId(this.reservationId).pipe(catchError(() => of(null))),
    }).subscribe({
      next: ({ reservation, payment }) => {
        if (this.toastBaselineSet && prevRes) {
          this.maybeEmitTransitionToasts(prevRes, prevSession, reservation, payment);
        }
        this.reservation = reservation;
        this.paymentSession = payment;
        this.syncPaymentVerifyAck(prevSession, payment);
      },
      error: () => undefined,
    });
  }

  private syncPaymentVerifyAck(
    prevSession: PaymentSession | null,
    nextSession: PaymentSession | null,
  ): void {
    if (nextSession?.status === 'INITIATED' && prevSession?.status !== 'INITIATED') {
      this.paymentVerifyAcknowledged = false;
    }
  }

  private maybeEmitTransitionToasts(
    prevRes: Reservation,
    prevSession: PaymentSession | null,
    nextRes: Reservation,
    nextSession: PaymentSession | null,
  ): void {
    if (nextRes.status === 'CONFIRMED' && prevRes.status !== 'CONFIRMED') {
      this.toast.success('Réservation confirmée — paiement vérifié.', 'Paiement');
      return;
    }
    if (nextSession?.status === 'COMPLETED' && prevSession?.status !== 'COMPLETED') {
      this.toast.success('Paiement complété côté prestataire simulé.', 'Paiement');
      return;
    }
    if (nextRes.paymentStatus === 'PAID' && prevRes.paymentStatus !== 'PAID') {
      this.toast.success('Statut paiement : payé.', 'Paiement');
      return;
    }
    if (nextSession?.status === 'FAILED' && prevSession?.status !== 'FAILED') {
      this.toast.danger('Le paiement est en échec.', 'Paiement');
      return;
    }
    if (nextRes.status === 'FAILED' && prevRes.status !== 'FAILED') {
      this.toast.danger('La réservation est en échec (paiement).', 'Réservation');
    }
  }

  private errMessage(err: unknown, fallback: string): string {
    const e = err as { error?: { message?: string } };
    return e?.error?.message || fallback;
  }

  get isAdmin(): boolean {
    return this.roles.isAdmin;
  }

  get canCancel(): boolean {
    if (!this.reservation || this.reservation.status !== 'PENDING') return false;
    return this.roles.canCancelReservation(this.reservation.userId);
  }

  get canConfirm(): boolean {
    if (!this.reservation || this.reservation.status !== 'PENDING') return false;
    return this.isAdmin;
  }

  get canResumePayment(): boolean {
    return (
      !!this.paymentSession &&
      this.reservation?.status === 'PENDING' &&
      this.paymentSession.status === 'INITIATED'
    );
  }

  get canRetryPayment(): boolean {
    return !!this.paymentSession && this.reservation?.status === 'FAILED';
  }

  /** Guided verification UI for simulated PSP finalize or retry */
  get needsPaymentAck(): boolean {
    return this.canResumePayment || this.canRetryPayment;
  }

  get paymentActionsBlocked(): boolean {
    return this.actionLoading || (this.needsPaymentAck && !this.paymentVerifyAcknowledged);
  }

  get paymentAmountMismatch(): boolean {
    const s = this.paymentSession;
    if (!s || s.amount == null || !this.reservation) return false;
    const t = this.reservation.totalPrice;
    if (t == null) return false;
    return Math.abs(s.amount - t) > 0.02;
  }

  get statusLabel(): string {
    if (this.reservation?.status === 'PENDING' && this.paymentSession?.status === 'INITIATED') {
      return 'PROCESSING';
    }
    return this.reservation?.status || '';
  }

  confirm(): void {
    if (!this.reservation) return;
    this.actionError = '';
    this.reservationService.confirm(this.reservation.id).subscribe({
      next: (data) => {
        this.reservation = data;
        this.toast.success('Réservation confirmée (admin).', 'Admin');
      },
      error: (err) => {
        const msg = this.errMessage(err, 'Échec de la confirmation.');
        this.actionError = msg;
        this.toast.danger(msg, 'Réservation');
      },
    });
  }

  resumePayment(): void {
    if (!this.paymentSession || this.paymentActionsBlocked) return;
    this.actionLoading = true;
    this.actionError = '';
    this.paymentService.callback(this.paymentSession.paymentId, { success: true }).subscribe({
      next: () => {
        this.actionLoading = false;
        this.toast.success('Paiement finalisé — en attente de confirmation de la réservation.', 'Paiement');
        this.refreshAsyncState();
      },
      error: (err) => {
        this.actionLoading = false;
        const msg = this.errMessage(err, 'Impossible de reprendre le paiement.');
        this.actionError = msg;
        this.toast.danger(msg, 'Paiement');
      },
    });
  }

  markPaymentFailed(): void {
    if (!this.paymentSession || this.paymentActionsBlocked) return;
    this.actionLoading = true;
    this.actionError = '';
    this.paymentService
      .callback(this.paymentSession.paymentId, { success: false, failureReason: 'Paiement refuse' })
      .subscribe({
        next: () => {
          this.actionLoading = false;
          this.toast.warning('Paiement simulé en échec.', 'Paiement');
          this.refreshAsyncState();
        },
        error: (err) => {
          this.actionLoading = false;
          const msg = this.errMessage(err, 'Impossible de marquer le paiement en échec.');
          this.actionError = msg;
          this.toast.danger(msg, 'Paiement');
        },
      });
  }

  retryPayment(): void {
    if (!this.paymentSession || this.paymentActionsBlocked) return;
    this.actionLoading = true;
    this.actionError = '';
    this.paymentService.retry(this.paymentSession.paymentId).subscribe({
      next: (session) => {
        this.paymentSession = session;
        this.paymentService.callback(session.paymentId, { success: true }).subscribe({
          next: () => {
            this.actionLoading = false;
            this.toast.success('Nouvelle tentative de paiement réussie.', 'Paiement');
            this.refreshAsyncState();
          },
          error: (err) => {
            this.actionLoading = false;
            const msg = this.errMessage(err, 'Échec de la reprise du paiement.');
            this.actionError = msg;
            this.toast.danger(msg, 'Paiement');
          },
        });
      },
      error: (err) => {
        this.actionLoading = false;
        const msg = this.errMessage(err, 'Impossible de lancer le retry paiement.');
        this.actionError = msg;
        this.toast.danger(msg, 'Paiement');
      },
    });
  }

  openCancel(): void {
    this.showCancelPanel = true;
    this.cancelReason = '';
    this.actionError = '';
  }

  closeCancel(): void {
    this.showCancelPanel = false;
  }

  submitCancel(): void {
    if (!this.reservation) return;
    const reason = this.cancelReason.trim() || 'Annulation utilisateur';
    this.actionError = '';
    this.reservationService.cancel(this.reservation.id, reason).subscribe({
      next: (data) => {
        this.reservation = data;
        this.showCancelPanel = false;
        this.toast.warning('Réservation annulée.', 'Réservation');
      },
      error: (err) => {
        const msg = this.errMessage(err, "Échec de l'annulation.");
        this.actionError = msg;
        this.toast.danger(msg, 'Réservation');
      },
    });
  }

  statusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED':
        return 'ija-badge--success';
      case 'PENDING':
        return 'ija-badge--warning';
      case 'CANCELLED':
        return 'ija-badge--danger';
      case 'FAILED':
        return 'ija-badge--danger';
      case 'PROCESSING':
        return 'ija-badge--info';
      default:
        return 'ija-badge--muted';
    }
  }

  paymentClass(status: string): string {
    switch (status) {
      case 'PAID':
        return 'ija-badge--success';
      case 'PENDING':
        return 'ija-badge--warning';
      case 'REFUNDED':
        return 'ija-badge--info';
      case 'FAILED':
        return 'ija-badge--danger';
      default:
        return 'ija-badge--muted';
    }
  }
}

import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { interval } from 'rxjs';
import { DataService } from '../../../shared/services/data.service';
import { AuthService } from '../../../core/services/auth.service';
import { RoleUiService } from '../../../core/services/role-ui.service';
import { UserLookupService } from '../../../shared/services/user-lookup.service';
import { FeedbackService } from '../../../shared/services/feedback.service';
import { ReservationService } from '../../../shared/services/reservation.service';
import { Event } from '../../../models/event';
import { Feedback, FeedbackStats } from '../../../models/feedback';
import { Reservation } from '../../../models/reservation';

@Component({
  standalone: false,
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css'],
})
export class EventDetailsComponent implements OnInit {
  event!: Event;
  loading = true;
  error = '';
  activeTab: 'about' | 'feedback' | 'reservations' = 'about';
  infoCollapsed = false;

  organizerName = '';
  feedbacks: Feedback[] = [];
  feedbackStats: FeedbackStats | null = null;
  eventReservations: Reservation[] = [];
  feedbackLoading = false;
  feedbackSubmitting = false;
  feedbackError = '';
  newRating = 5;
  newComment = '';

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dataService: DataService,
    public auth: AuthService,
    public roles: RoleUiService,
    private userLookup: UserLookupService,
    private feedbackService: FeedbackService,
    private reservationService: ReservationService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    if (!id) {
      this.router.navigate(['/events']);
      return;
    }
    this.dataService.getById(id).subscribe({
      next: (event) => {
        this.event = event;
        this.loading = false;
        this.loadOrganizer();
        this.loadTabData();
        this.startPolling();
      },
      error: () => {
        this.loading = false;
        this.router.navigate(['/events']);
      },
    });
  }

  private startPolling(): void {
    interval(45000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (!this.event || document.visibilityState !== 'visible') return;
        if (this.activeTab === 'feedback' || this.activeTab === 'reservations') {
          this.loadTabData();
        }
      });
  }

  private loadOrganizer(): void {
    this.userLookup.getById(this.event.organizerId).subscribe((u) => {
      this.organizerName = u?.username ? `@${u.username}` : `Organisateur #${this.event.organizerId}`;
    });
  }

  selectTab(tab: 'about' | 'feedback' | 'reservations'): void {
    this.activeTab = tab;
    this.loadTabData();
  }

  loadTabData(): void {
    if (!this.event) return;
    if (this.activeTab === 'feedback') {
      this.feedbackLoading = true;
      this.feedbackService.getByEvent(this.event.id).subscribe({
        next: (rows) => {
          this.feedbacks = rows;
          this.feedbackLoading = false;
        },
        error: () => (this.feedbackLoading = false),
      });
      this.feedbackService.getStatsByEvent(this.event.id).subscribe({
        next: (s) => (this.feedbackStats = s),
        error: () => (this.feedbackStats = null),
      });
    }
    if (this.activeTab === 'reservations' && this.roles.canViewEventReservationsTab(this.event)) {
      this.reservationService.getByEvent(this.event.id).subscribe({
        next: (rows) => (this.eventReservations = rows),
        error: () => (this.eventReservations = []),
      });
    }
  }

  get isOwner(): boolean {
    return this.roles.isEventOrganizer(this.event);
  }

  get isAdmin(): boolean {
    return this.roles.isAdmin;
  }

  get canEdit(): boolean {
    return this.isOwner || this.isAdmin;
  }

  get showReservationsTab(): boolean {
    return this.roles.canViewEventReservationsTab(this.event);
  }

  toggleInfo(): void {
    this.infoCollapsed = !this.infoCollapsed;
  }

  like(): void {
    this.dataService.likeEvent(this.event.id).subscribe({
      next: (updated) => (this.event = updated),
    });
  }

  deleteEvent(): void {
    if (confirm('Voulez-vous vraiment supprimer cet evenement ?')) {
      this.dataService.deleteEvent(this.event.id).subscribe({
        next: () => this.router.navigate(['/events']),
        error: (err) => (this.error = err?.error?.message || 'Erreur lors de la suppression.'),
      });
    }
  }

  dateExpired(): boolean {
    return new Date(this.event.date) < new Date();
  }

  submitFeedback(): void {
    if (!this.auth.isLoggedIn || !this.newComment.trim()) {
      this.feedbackError = 'Connectez-vous et saisissez un commentaire.';
      return;
    }
    this.feedbackSubmitting = true;
    this.feedbackError = '';
    const userId = this.auth.currentUser?.id ?? 0;
    this.feedbackService
      .create({
        eventId: this.event.id,
        userId,
        rating: this.newRating,
        comment: this.newComment.trim(),
      })
      .subscribe({
        next: () => {
          this.newComment = '';
          this.newRating = 5;
          this.feedbackSubmitting = false;
          this.loadTabData();
        },
        error: (err) => {
          this.feedbackSubmitting = false;
          this.feedbackError = err?.error?.message || 'Impossible d\'envoyer l\'avis.';
        },
      });
  }

  starsPreview(n: number): number[] {
    return Array.from({ length: 5 }, (_, i) => (i < n ? 1 : 0));
  }

  feedbackStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'ija-badge--success';
      case 'REJECTED':
      case 'FLAGGED':
        return 'ija-badge--danger';
      default:
        return 'ija-badge--warning';
    }
  }

  reservationStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED':
        return 'ija-badge--success';
      case 'PENDING':
        return 'ija-badge--warning';
      case 'CANCELLED':
        return 'ija-badge--danger';
      default:
        return 'ija-badge--muted';
    }
  }

  paymentBadgeClass(status: string): string {
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

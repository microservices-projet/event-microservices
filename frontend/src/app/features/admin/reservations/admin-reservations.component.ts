import { Component, OnInit } from '@angular/core';
import { Reservation } from '../../../models/reservation';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-admin-reservations',
  templateUrl: './admin-reservations.component.html',
  styleUrls: ['./admin-reservations.component.css'],
})
export class AdminReservationsComponent implements OnInit {
  reservations: Reservation[] = [];
  loading = true;
  loadError = '';
  userId = '';
  eventId = '';
  status = '';
  paymentStatus = '';

  constructor(
    private read: AdminReadService,
    private write: AdminWriteService,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.userId = '';
    this.eventId = '';
    this.status = '';
    this.paymentStatus = '';
    this.reload();
  }

  get hasActiveFilters(): boolean {
    return !!(this.userId.trim() || this.eventId.trim() || this.status.trim() || this.paymentStatus.trim());
  }

  reload(): void {
    this.loading = true;
    this.loadError = '';
    const parsedUserId = this.parsePositiveInt(this.userId);
    const parsedEventId = this.parsePositiveInt(this.eventId);
    this.read.getReservations({
      userId: parsedUserId,
      eventId: parsedEventId,
      status: this.status.trim(),
      paymentStatus: this.paymentStatus.trim(),
    }).subscribe({
      next: (data) => { this.reservations = data; this.loading = false; },
      error: () => {
        this.loadError = 'Unable to load reservations right now.';
        this.reservations = [];
        this.loading = false;
      },
    });
  }

  cancel(r: Reservation): void {
    const reason = prompt('Reason to cancel reservation');
    if (!reason) return;
    const cancelledBy = this.auth.currentUser?.id ?? 0;
    this.write.cancelReservation(r.id, reason, cancelledBy).subscribe({ next: () => this.reload() });
  }

  private parsePositiveInt(raw: string): number | null {
    const trimmed = raw.trim();
    if (!trimmed) return null;
    const parsed = Number(trimmed);
    if (!Number.isInteger(parsed) || parsed <= 0) return null;
    return parsed;
  }
}

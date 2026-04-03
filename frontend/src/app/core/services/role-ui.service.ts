import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { Event } from '../../models/event';

/**
 * Aligns UI with high-level roles: USER, MODERATOR (organizer), ADMIN.
 */
@Injectable({ providedIn: 'root' })
export class RoleUiService {
  constructor(private auth: AuthService) {}

  get isAdmin(): boolean {
    return this.auth.currentUser?.role === 'ADMIN';
  }

  /** Organizer capabilities in this app map to MODERATOR. */
  get isOrganizer(): boolean {
    return this.auth.currentUser?.role === 'MODERATOR';
  }

  get isLoggedIn(): boolean {
    return this.auth.isLoggedIn;
  }

  canAccessAdminRoutes(): boolean {
    return this.isAdmin;
  }

  /** Create / edit own events (logged-in users; ownership enforced on detail/edit). */
  canCreateOrEditEvents(): boolean {
    return this.isLoggedIn;
  }

  canViewTicketsSection(): boolean {
    return this.isAdmin || this.isOrganizer;
  }

  isEventOrganizer(event: Pick<Event, 'organizerId'> | null | undefined): boolean {
    const u = this.auth.currentUser;
    return !!event && !!u && u.id > 0 && event.organizerId === u.id;
  }

  canViewEventReservationsTab(event: Pick<Event, 'organizerId'> | null | undefined): boolean {
    return this.isAdmin || this.isEventOrganizer(event);
  }

  canCancelReservation(reservationUserId: number): boolean {
    if (this.isAdmin) return true;
    const u = this.auth.currentUser;
    return !!u && u.id > 0 && reservationUserId === u.id;
  }
}

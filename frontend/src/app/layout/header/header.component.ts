import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { UserResponse } from '../../models/user';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  user: UserResponse | null = null;
  menuOpen = false;
  private sub!: Subscription;

  constructor(public auth: AuthService, private router: Router) {}

  get showCalendar(): boolean {
    // Always show in header (UX: day selection should be available anywhere).
    return true;
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  get isOrganizer(): boolean {
    return this.user?.role === 'MODERATOR';
  }

  ngOnInit(): void {
    this.sub = this.auth.user$.subscribe(u => this.user = u);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  logout(): void {
    this.menuOpen = false;
    this.auth.logout();
  }
}

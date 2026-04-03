import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { UserResponse } from '../../models/user';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
})
export class ProfileComponent implements OnInit {
  user: UserResponse | null = null;

  quickLinks = [
    { label: 'Mes Reservations', route: '/reservations', icon: 'calendar' },
    { label: 'Mes Tickets', route: '/tickets', icon: 'ticket' },
    { label: 'Mes Feedbacks', route: '/feedbacks', icon: 'message' },
    { label: 'Mes Reclamations', route: '/reclamations', icon: 'alert' },
  ];

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.user = this.auth.currentUser;
    if (!this.user) {
      this.router.navigate(['/auth/login']);
    }
  }

  logout(): void {
    this.auth.logout();
  }
}

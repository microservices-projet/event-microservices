import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';

@Component({
  standalone: false,
  selector: 'app-admin-shell',
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.css',
})
export class AdminShellComponent implements OnInit, OnDestroy {
  crumbLabel = 'Tableau de bord';
  private sub?: Subscription;

  private readonly labels: Record<string, string> = {
    dashboard: 'Tableau de bord',
    users: 'Utilisateurs',
    events: 'Événements',
    reservations: 'Réservations',
    tickets: 'Tickets',
    feedbacks: 'Retours',
    reclamations: 'Réclamations',
  };

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.syncCrumb();
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.syncCrumb());
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private syncCrumb(): void {
    const path =
      this.router.url
        .split('?')[0]
        .replace(/^\/admin\/?/, '')
        .split('/')
        .filter(Boolean)[0] || 'dashboard';
    this.crumbLabel = this.labels[path] ?? path;
  }
}

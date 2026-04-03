import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminShellComponent } from './shell/admin-shell.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './users/admin-users.component';
import { AdminEventsComponent } from './events/admin-events.component';
import { AdminReservationsComponent } from './reservations/admin-reservations.component';
import { AdminTicketsComponent } from './tickets/admin-tickets.component';
import { AdminFeedbacksComponent } from './feedbacks/admin-feedbacks.component';
import { AdminReclamationsComponent } from './reclamations/admin-reclamations.component';

const routes: Routes = [
  {
    path: '',
    component: AdminShellComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'events', component: AdminEventsComponent },
      { path: 'reservations', component: AdminReservationsComponent },
      { path: 'tickets', component: AdminTicketsComponent },
      { path: 'feedbacks', component: AdminFeedbacksComponent },
      { path: 'reclamations', component: AdminReclamationsComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminShellComponent } from './shell/admin-shell.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './users/admin-users.component';
import { AdminEventsComponent } from './events/admin-events.component';
import { AdminReservationsComponent } from './reservations/admin-reservations.component';
import { AdminTicketsComponent } from './tickets/admin-tickets.component';
import { AdminFeedbacksComponent } from './feedbacks/admin-feedbacks.component';
import { AdminReclamationsComponent } from './reclamations/admin-reclamations.component';

@NgModule({
  declarations: [
    AdminShellComponent,
    AdminDashboardComponent,
    AdminUsersComponent,
    AdminEventsComponent,
    AdminReservationsComponent,
    AdminTicketsComponent,
    AdminFeedbacksComponent,
    AdminReclamationsComponent,
  ],
  imports: [CommonModule, FormsModule, AdminRoutingModule],
})
export class AdminModule {}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ReservationsRoutingModule } from './reservations-routing.module';
import { ReservationListComponent } from './reservation-list/reservation-list.component';
import { ReservationCreateComponent } from './reservation-create/reservation-create.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';

@NgModule({
  declarations: [ReservationListComponent, ReservationCreateComponent, ReservationDetailComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ReservationsRoutingModule],
})
export class ReservationsModule {}

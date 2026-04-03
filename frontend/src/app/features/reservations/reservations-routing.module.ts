import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReservationListComponent } from './reservation-list/reservation-list.component';
import { ReservationCreateComponent } from './reservation-create/reservation-create.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';

const routes: Routes = [
  { path: '', component: ReservationListComponent },
  { path: 'create', component: ReservationCreateComponent },
  { path: ':id', component: ReservationDetailComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReservationsRoutingModule {}

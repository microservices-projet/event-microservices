import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './layout/home/home.component';
import { NotfoundComponent } from './notfound/notfound.component';
import { AdminGuard } from './core/guards/admin.guard';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule),
  },
  {
    path: 'events',
    loadChildren: () => import('./features/events/events.module').then(m => m.EventsModule),
  },
  {
    path: 'tickets',
    loadChildren: () => import('./features/tickets/tickets.module').then(m => m.TicketsModule),
  },
  {
    path: 'feedbacks',
    loadChildren: () => import('./features/feedbacks/feedbacks.module').then(m => m.FeedbacksModule),
  },
  {
    path: 'reclamations',
    loadChildren: () => import('./features/reclamations/reclamations.module').then(m => m.ReclamationsModule),
  },
  {
    path: 'reservations',
    loadChildren: () => import('./features/reservations/reservations.module').then(m => m.ReservationsModule),
  },
  {
    path: 'profil',
    loadChildren: () => import('./features/profile/profile.module').then(m => m.ProfileModule),
  },
  {
    path: 'admin',
    canActivate: [AdminGuard],
    loadChildren: () => import('./features/admin/admin.module').then(m => m.AdminModule),
  },
  { path: '**', component: NotfoundComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}

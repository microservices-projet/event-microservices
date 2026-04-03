import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReclamationListComponent } from './reclamation-list/reclamation-list.component';
import { ReclamationCreateComponent } from './reclamation-create/reclamation-create.component';
import { ReclamationDetailComponent } from './reclamation-detail/reclamation-detail.component';

const routes: Routes = [
  { path: '', component: ReclamationListComponent },
  { path: 'create', component: ReclamationCreateComponent },
  { path: ':id', component: ReclamationDetailComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReclamationsRoutingModule {}

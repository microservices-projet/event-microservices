import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ReclamationsRoutingModule } from './reclamations-routing.module';
import { ReclamationListComponent } from './reclamation-list/reclamation-list.component';
import { ReclamationCreateComponent } from './reclamation-create/reclamation-create.component';
import { ReclamationDetailComponent } from './reclamation-detail/reclamation-detail.component';

@NgModule({
  declarations: [ReclamationListComponent, ReclamationCreateComponent, ReclamationDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, ReclamationsRoutingModule],
})
export class ReclamationsModule {}

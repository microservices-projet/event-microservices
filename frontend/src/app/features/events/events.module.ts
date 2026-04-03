import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { EventsRoutingModule } from './events-routing.module';
import { EventsComponent } from './events.component';
import { ListEventComponent } from './list-event/list-event.component';
import { EventDetailsComponent } from './event-details/event-details.component';
import { CardComponent } from './card/card.component';
import { AddEventComponent } from './add-event/add-event.component';
import { ParticipationFormComponent } from './participation-form/participation-form.component';
import { TicketPurchaseCardComponent } from './ticket-purchase-card/ticket-purchase-card.component';
import { CapitalizeFirstPipe } from '../../shared/pipes/capitalize-first.pipe';

@NgModule({
  declarations: [
    EventsComponent,
    ListEventComponent,
    EventDetailsComponent,
    CardComponent,
    AddEventComponent,
    ParticipationFormComponent,
    TicketPurchaseCardComponent,
    CapitalizeFirstPipe,
  ],
  imports: [CommonModule, EventsRoutingModule, FormsModule, ReactiveFormsModule],
})
export class EventsModule {}

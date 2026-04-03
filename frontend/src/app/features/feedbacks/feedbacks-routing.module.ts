import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FeedbackListComponent } from './feedback-list/feedback-list.component';
import { FeedbackAddComponent } from './feedback-add/feedback-add.component';
import { FeedbackStatsComponent } from './feedback-stats/feedback-stats.component';

const routes: Routes = [
  { path: '', component: FeedbackListComponent },
  { path: 'add', component: FeedbackAddComponent },
  { path: 'stats/:eventId', component: FeedbackStatsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FeedbacksRoutingModule {}

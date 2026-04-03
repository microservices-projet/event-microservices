import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { FeedbacksRoutingModule } from './feedbacks-routing.module';
import { FeedbackListComponent } from './feedback-list/feedback-list.component';
import { FeedbackAddComponent } from './feedback-add/feedback-add.component';
import { FeedbackStatsComponent } from './feedback-stats/feedback-stats.component';

@NgModule({
  declarations: [FeedbackListComponent, FeedbackAddComponent, FeedbackStatsComponent],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, FeedbacksRoutingModule],
})
export class FeedbacksModule {}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { TruncatePipe } from './pipes/truncate.pipe';
import { StatusBadgePipe } from './pipes/status-badge.pipe';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';

@NgModule({
  declarations: [
    TimeAgoPipe,
    TruncatePipe,
    StatusBadgePipe,
    LoadingSpinnerComponent,
    EmptyStateComponent,
  ],
  imports: [CommonModule],
  exports: [
    TimeAgoPipe,
    TruncatePipe,
    StatusBadgePipe,
    LoadingSpinnerComponent,
    EmptyStateComponent,
  ],
})
export class SharedModule {}

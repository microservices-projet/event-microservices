import { Component, OnInit } from '@angular/core';
import { Reclamation } from '../../../models/reclamation';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-admin-reclamations',
  templateUrl: './admin-reclamations.component.html',
  styleUrls: ['./admin-reclamations.component.css'],
})
export class AdminReclamationsComponent implements OnInit {
  reclamations: Reclamation[] = [];
  loading = true;
  userId = '';
  eventId = '';
  status = '';
  type = '';

  constructor(
    private read: AdminReadService,
    private write: AdminWriteService,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.userId = '';
    this.eventId = '';
    this.status = '';
    this.type = '';
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.read.getReclamations({
      userId: this.userId ? Number(this.userId) : null,
      eventId: this.eventId ? Number(this.eventId) : null,
      status: this.status,
      type: this.type,
    }).subscribe({
      next: (data) => { this.reclamations = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  assign(rec: Reclamation): void {
    const currentId = this.auth.currentUser?.id ?? 0;
    this.write.assignReclamation(rec.id, currentId).subscribe({ next: () => this.reload() });
  }

  resolve(rec: Reclamation): void {
    this.write.respondReclamation(rec.id, 'Handled by admin').subscribe({
      next: () => this.write.updateReclamationStatus(rec.id, 'RESOLVED').subscribe({ next: () => this.reload() }),
    });
  }
}

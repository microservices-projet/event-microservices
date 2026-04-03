import { Component, OnInit } from '@angular/core';
import { ReclamationService } from '../../../shared/services/reclamation.service';
import { Reclamation } from '../../../models/reclamation';

@Component({
  standalone: false,
  selector: 'app-reclamation-list',
  templateUrl: './reclamation-list.component.html',
  styleUrls: ['./reclamation-list.component.css'],
})
export class ReclamationListComponent implements OnInit {
  reclamations: Reclamation[] = [];
  loading = true;
  filterStatus = '';

  constructor(private reclamationService: ReclamationService) {}

  ngOnInit(): void {
    this.reclamationService.getAll().subscribe({
      next: (data) => { this.reclamations = data; this.loading = false; },
      error: () => this.loading = false,
    });
  }

  get filteredReclamations(): Reclamation[] {
    if (!this.filterStatus) return this.reclamations;
    return this.reclamations.filter(r => r.status === this.filterStatus);
  }

  statusClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'ija-badge--info';
      case 'IN_PROGRESS': return 'ija-badge--warning';
      case 'RESOLVED': return 'ija-badge--success';
      case 'CLOSED': return 'ija-badge--muted';
      case 'REJECTED': return 'ija-badge--danger';
      default: return 'ija-badge--muted';
    }
  }

  priorityClass(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return 'ija-badge--danger';
      case 'HIGH': return 'ija-badge--warning';
      case 'MEDIUM': return 'ija-badge--info';
      default: return 'ija-badge--muted';
    }
  }
}

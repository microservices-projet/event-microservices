import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReclamationService } from '../../../shared/services/reclamation.service';
import { Reclamation } from '../../../models/reclamation';

@Component({
  standalone: false,
  selector: 'app-reclamation-detail',
  templateUrl: './reclamation-detail.component.html',
  styleUrls: ['./reclamation-detail.component.css'],
})
export class ReclamationDetailComponent implements OnInit {
  reclamation: Reclamation | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recService: ReclamationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    this.recService.getById(id).subscribe({
      next: (data) => { this.reclamation = data; this.loading = false; },
      error: () => { this.loading = false; this.router.navigate(['/reclamations']); },
    });
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
}

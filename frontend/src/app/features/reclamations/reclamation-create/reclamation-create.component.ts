import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ReclamationService } from '../../../shared/services/reclamation.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-reclamation-create',
  templateUrl: './reclamation-create.component.html',
  styleUrls: ['./reclamation-create.component.css'],
})
export class ReclamationCreateComponent implements OnInit {
  form: FormGroup;
  loading = false;
  error = '';

  types = ['EVENT_ISSUE', 'RESERVATION_ISSUE', 'TICKET_ISSUE', 'PAYMENT_ISSUE', 'OTHER'];

  constructor(
    private fb: FormBuilder,
    private recService: ReclamationService,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      subject: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      type: ['OTHER', Validators.required],
      eventId: [''],
      ticketId: [''],
    });
  }

  ngOnInit(): void {
    if (!this.auth.isLoggedIn) {
      void this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/reclamations/create' } });
    }
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;
    const userId = this.auth.currentUser?.id;
    if (userId == null || userId < 0 || !Number.isFinite(userId)) {
      this.error = 'Connectez-vous avec un compte valide (identifiant utilisateur manquant).';
      void this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/reclamations/create' } });
      return;
    }
    this.loading = true;
    this.error = '';
    const val = this.form.value;
    this.recService
      .create({
        userId,
        subject: val.subject.trim(),
        description: val.description.trim(),
        type: val.type,
        eventId: this.optionalPositiveLong(val.eventId),
        ticketId: this.optionalPositiveLong(val.ticketId),
      })
      .subscribe({
        next: () => void this.router.navigate(['/reclamations']),
        error: (err: HttpErrorResponse) => {
          this.error = this.formatError(err);
          this.loading = false;
        },
      });
  }

  private optionalPositiveLong(raw: unknown): number | undefined {
    if (raw === '' || raw === null || raw === undefined) return undefined;
    const n = Number(raw);
    if (!Number.isFinite(n) || n < 1) return undefined;
    return n;
  }

  private formatError(err: HttpErrorResponse): string {
    const body = err.error;
    if (body && typeof body === 'object' && 'message' in body && typeof (body as { message: unknown }).message === 'string') {
      return (body as { message: string }).message;
    }
    if (typeof body === 'string' && body.length > 0) return body;
    if (err.status === 401 || err.status === 403) {
      return 'Session invalide ou acces refuse. Reconnectez-vous.';
    }
    if (err.status === 0) {
      return 'Impossible de joindre le serveur (gateway ou reclamation-service).';
    }
    return 'Erreur lors de la creation.';
  }
}

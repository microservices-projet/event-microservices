import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { FeedbackService } from '../../../shared/services/feedback.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-feedback-add',
  templateUrl: './feedback-add.component.html',
  styleUrls: ['./feedback-add.component.css'],
})
export class FeedbackAddComponent {
  form: FormGroup;
  loading = false;
  error = '';
  selectedRating = 0;
  hoverRating = 0;

  constructor(
    private fb: FormBuilder,
    private feedbackService: FeedbackService,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      eventId: ['', [Validators.required, Validators.min(1)]],
      comment: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  setRating(val: number): void {
    this.selectedRating = val;
  }

  onSubmit(): void {
    if (this.form.invalid || this.selectedRating === 0 || this.loading) return;
    this.loading = true;
    this.error = '';
    const userId = this.auth.currentUser?.id || 1;
    this.feedbackService.create({
      eventId: Number(this.form.value.eventId),
      userId,
      rating: this.selectedRating,
      comment: this.form.value.comment,
    }).subscribe({
      next: () => this.router.navigate(['/feedbacks']),
      error: (err) => {
        this.error = err?.error?.message || err?.error || 'Erreur lors de l\'envoi.';
        this.loading = false;
      },
    });
  }
}

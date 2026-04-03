import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  form: FormGroup;
  error = '';
  loading = false;
  ssoLoading = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    if (this.auth.isLoggedIn) {
      this.router.navigate(['/events']);
    }
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(4)]],
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading || this.ssoLoading) return;
    this.error = '';
    this.loading = true;
    const { username, email, password } = this.form.value;
    this.auth.register(username, email, password).subscribe({
      next: (user) => {
        if (user.accessToken) {
          this.router.navigate(['/events']);
        } else {
          this.router.navigate(['/auth/login']);
        }
      },
      error: (err) => {
        this.error = this.parseError(err, 'Erreur lors de la creation du compte.');
        this.loading = false;
      },
    });
  }

  onSsoRegister(): void {
    if (this.loading || this.ssoLoading) return;
    this.error = '';
    this.ssoLoading = true;
    this.auth.registerWithKeycloak().subscribe({
      next: () => {},
      error: (err) => {
        this.error = this.parseError(err, 'Erreur lors de la redirection vers Keycloak.');
        this.ssoLoading = false;
      },
    });
  }

  private parseError(err: any, fallback: string): string {
    if (err?.error?.message) return err.error.message;
    if (typeof err?.error === 'string') return err.error;
    if (err?.status === 0) return 'Impossible de contacter le serveur.';
    return fallback;
  }
}

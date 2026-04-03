import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  form: FormGroup;
  error = '';
  loading = false;
  ssoLoading = false;
  private returnUrl: string;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    if (this.auth.isLoggedIn) {
      this.router.navigate(['/events']);
    }
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/events';
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]],
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading || this.ssoLoading) return;
    this.error = '';
    this.loading = true;
    const { username, password } = this.form.value;
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl),
      error: (err) => {
        this.error = this.parseError(err, 'Nom d’utilisateur ou mot de passe invalide.');
        this.loading = false;
      },
    });
  }

  onSsoLogin(): void {
    if (this.loading || this.ssoLoading) return;
    this.error = '';
    this.ssoLoading = true;
    this.auth.loginWithKeycloak().subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl),
      error: (err) => {
        this.error = this.parseError(err, 'Echec d’authentification Keycloak.');
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

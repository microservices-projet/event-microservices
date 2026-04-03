import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  BehaviorSubject,
  catchError,
  firstValueFrom,
  from,
  map,
  Observable,
  of,
  switchMap,
  tap,
  timeout,
} from 'rxjs';
import { LoginRequest, RegisterRequest, UserResponse } from '../../models/user';
import { KeycloakService } from './keycloak.service';

/** Shared with {@link KeycloakTokenInterceptor} for Bearer (local JWT). */
export const AUTH_USER_STORAGE_KEY = 'auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userSubject = new BehaviorSubject<UserResponse | null>(this.loadUser());
  user$ = this.userSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private keycloak: KeycloakService
  ) {}

  private loadUser(): UserResponse | null {
    try {
      const raw = localStorage.getItem(AUTH_USER_STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  get isLoggedIn(): boolean {
    return !!this.userSubject.value;
  }

  get currentUser(): UserResponse | null {
    return this.userSubject.value;
  }

  login(username: string, password: string): Observable<UserResponse> {
    return this.http.post<UserResponse>('/api/users/login', { username, password } as LoginRequest).pipe(
      tap((user) => this.setUser(user)),
    );
  }

  loginWithKeycloak(): Observable<UserResponse> {
    return from(this.keycloak.login()).pipe(
      switchMap(() => {
        const base = this.buildUserFromKeycloak();
        if (!base) throw new Error('Keycloak login failed');
        return this.http.get<UserResponse>('/api/users/profile/me').pipe(
          tap((profile) =>
            this.setUser({
              ...base,
              id: profile.id,
              email: profile.email || base.email,
              role: profile.role,
              status: profile.status,
            }),
          ),
          map((profile) => ({
            ...base,
            id: profile.id,
            email: profile.email || base.email,
            role: profile.role,
            status: profile.status,
          })),
          catchError(() => {
            this.setUser(base);
            return of(base);
          }),
        );
      }),
    );
  }

  register(username: string, email: string, password: string): Observable<UserResponse> {
    return this.http.post<UserResponse>('/api/users', { username, email, password } as RegisterRequest).pipe(
      tap((user) => {
        if (user.accessToken) {
          this.setUser(user);
        }
      }),
    );
  }

  registerWithKeycloak(): Observable<void> {
    return from(this.keycloak.register());
  }

  logout(): void {
    if (this.keycloak.authenticated) {
      void this.keycloak.logout();
    }
    localStorage.removeItem(AUTH_USER_STORAGE_KEY);
    this.userSubject.next(null);
    this.router.navigate(['/']);
  }

  async hydrateFromKeycloak(): Promise<void> {
    const base = this.buildUserFromKeycloak();
    if (base) {
      this.setUser(base);
      try {
        const profile = await firstValueFrom(
          this.http.get<UserResponse>('/api/users/profile/me').pipe(
            timeout(8000),
            catchError(() => of(undefined)),
          ),
        );
        if (profile) {
          this.setUser({
            ...base,
            id: profile.id,
            email: profile.email || base.email,
            role: profile.role,
            status: profile.status,
          });
        }
      } catch {
        // Pas de compte local avec le meme username : id peut rester 0, le backend resout a la reservation.
      }
    } else {
      const existing = this.loadUser();
      if (existing?.accessToken) {
        this.userSubject.next(existing);
        return;
      }
      localStorage.removeItem(AUTH_USER_STORAGE_KEY);
      this.userSubject.next(null);
    }
  }

  private setUser(user: UserResponse): void {
    localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(user));
    this.userSubject.next(user);
  }

  private buildUserFromKeycloak(): UserResponse | null {
    if (!this.keycloak.authenticated) return null;
    const normalizedRoles = this.keycloak.roles.map((r) => r.toUpperCase());
    const role = normalizedRoles.includes('ADMIN') || normalizedRoles.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
    return {
      id: 0,
      username: this.keycloak.username ?? 'keycloak-user',
      email: this.keycloak.email ?? '',
      role,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    };
  }
}

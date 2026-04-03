import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { from, Observable, switchMap } from 'rxjs';
import { AUTH_USER_STORAGE_KEY } from '../services/auth.service';
import { KeycloakService } from '../services/keycloak.service';

@Injectable()
export class KeycloakTokenInterceptor implements HttpInterceptor {
  constructor(private keycloak: KeycloakService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Local register/login must not send a stale Keycloak Bearer (gateway validates JWT on all /api/** except these).
    if (this.isLocalAuthRequest(req)) {
      return next.handle(req);
    }
    return from(this.keycloak.getValidToken()).pipe(
      switchMap((token) => {
        const bearer = token ?? this.localJwtFromStorage();
        if (!bearer) return next.handle(req);
        return next.handle(
          req.clone({
            setHeaders: {
              Authorization: `Bearer ${bearer}`,
            },
          }),
        );
      }),
    );
  }

  private isLocalAuthRequest(req: HttpRequest<unknown>): boolean {
    const url = req.url.split('?')[0];
    if (req.method === 'POST' && url === '/api/users') return true;
    if (req.method === 'POST' && url === '/api/users/login') return true;
    return false;
  }

  /** Local username/password login stores JWT in the same object as {@link AuthService} */
  private localJwtFromStorage(): string | null {
    try {
      const raw = localStorage.getItem(AUTH_USER_STORAGE_KEY);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as { accessToken?: string };
      return typeof parsed.accessToken === 'string' && parsed.accessToken.length > 0 ? parsed.accessToken : null;
    } catch {
      return null;
    }
  }
}

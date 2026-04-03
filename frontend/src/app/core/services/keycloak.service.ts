import { Injectable } from '@angular/core';
import Keycloak, { KeycloakInitOptions, KeycloakProfile } from 'keycloak-js';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private keycloak = new Keycloak({
    url: environment.keycloakUrl,
    realm: 'event-app',
    clientId: 'event-frontend',
  });

  async init(): Promise<boolean> {
    const options: KeycloakInitOptions = {
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    };
    const timeoutMs = 10_000;
    try {
      return await Promise.race([
        this.keycloak.init(options),
        new Promise<boolean>((_, reject) =>
          setTimeout(() => reject(new Error('Keycloak init timeout')), timeoutMs)),
      ]);
    } catch {
      return false;
    }
  }

  async login(): Promise<void> {
    const loginUrl = await this.keycloak.createLoginUrl({ redirectUri: window.location.origin });
    window.location.assign(loginUrl);
  }

  async register(): Promise<void> {
    const registerUrl = await this.keycloak.createRegisterUrl({ redirectUri: window.location.origin });
    window.location.assign(registerUrl);
  }

  async logout(): Promise<void> {
    await this.keycloak.logout({ redirectUri: window.location.origin });
  }

  async getValidToken(minValiditySeconds = 20): Promise<string | undefined> {
    if (!this.keycloak.authenticated) return undefined;
    try {
      await this.keycloak.updateToken(minValiditySeconds);
      return this.keycloak.token;
    } catch {
      return this.keycloak.token;
    }
  }

  get token(): string | undefined {
    return this.keycloak.token;
  }

  get authenticated(): boolean {
    return !!this.keycloak.authenticated;
  }

  get username(): string | undefined {
    return this.keycloak.tokenParsed?.['preferred_username'] as string | undefined;
  }

  get email(): string | undefined {
    return this.keycloak.tokenParsed?.['email'] as string | undefined;
  }

  get roles(): string[] {
    const parsed = this.keycloak.tokenParsed as { realm_access?: { roles?: string[] } } | undefined;
    return parsed?.realm_access?.roles ?? [];
  }

  async loadUserProfile(): Promise<KeycloakProfile | undefined> {
    if (!this.authenticated) return undefined;
    return this.keycloak.loadUserProfile();
  }
}

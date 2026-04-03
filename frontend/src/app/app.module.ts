import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './layout/home/home.component';
import { HeaderComponent } from './layout/header/header.component';
import { FooterComponent } from './layout/footer/footer.component';
import { CalendarStripComponent } from './layout/calendar-strip/calendar-strip.component';
import { NotfoundComponent } from './notfound/notfound.component';
import { ToastStackComponent } from './shared/components/toast-stack/toast-stack.component';
import { KeycloakService } from './core/services/keycloak.service';
import { AuthService } from './core/services/auth.service';
import { KeycloakTokenInterceptor } from './core/interceptors/keycloak-token.interceptor';

function initKeycloakFactory(keycloak: KeycloakService, auth: AuthService): () => Promise<void> {
  return async () => {
    const overallMs = 12_000;
    try {
      await Promise.race([
        (async () => {
          await keycloak.init();
          await auth.hydrateFromKeycloak();
        })(),
        new Promise<void>((_, reject) =>
          setTimeout(() => reject(new Error('Auth bootstrap timeout')), overallMs)),
      ]);
    } catch {
      // Keycloak/API indisponible ou lent : on affiche quand meme l’app.
    }
  };
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    HeaderComponent,
    CalendarStripComponent,
    FooterComponent,
    NotfoundComponent,
    ToastStackComponent,
  ],
  imports: [BrowserModule, BrowserAnimationsModule, AppRoutingModule, FormsModule],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: KeycloakTokenInterceptor,
      multi: true,
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initKeycloakFactory,
      deps: [KeycloakService, AuthService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}

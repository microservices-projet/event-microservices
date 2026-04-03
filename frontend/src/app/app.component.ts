import { DOCUMENT } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';

@Component({
  standalone: false,
  selector: 'app-comp',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'WorkshopTwin';
  adminShellActive = false;
  private routerSub?: Subscription;

  constructor(
    private readonly router: Router,
    @Inject(DOCUMENT) private readonly doc: Document,
  ) {}

  ngOnInit(): void {
    const body = this.doc.body;
    const apply = (url: string) => {
      const admin = /(^|\/)admin(\/|$)/.test(url);
      this.adminShellActive = admin;
      body.classList.toggle('admin-app-mode', admin);
    };
    apply(this.router.url);
    this.routerSub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => apply(e.urlAfterRedirects));
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }
}

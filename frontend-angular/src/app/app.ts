import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MsalBroadcastService, MsalService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { Subject, filter, takeUntil } from 'rxjs';

import { AuthService } from './auth.service';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {

  private msal = inject(MsalService);
  private broadcast = inject(MsalBroadcastService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  protected auth = inject(AuthService);

  private destroy$ = new Subject<void>();

  autenticado = false;

  ngOnInit(): void {
    this.msal
      .handleRedirectObservable({ navigateToLoginRequestUrl: false })
      .subscribe({
        next: result => {
          if (result?.account) {
            this.msal.instance.setActiveAccount(result.account);

            // Quita el #code= de la URL ANTES de navegar,
            // para que MsalGuard no intente procesarlo de nuevo.
            history.replaceState(null, '', window.location.pathname);

            this.actualizarEstado();
            this.router.navigateByUrl('/dashboard');
            return;
          }
          this.actualizarEstado();
        },
        error: err => console.error('Error MSAL:', err)
      });

    this.broadcast.inProgress$
      .pipe(
        filter((estado: InteractionStatus) => estado === InteractionStatus.None),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.actualizarEstado());
  }

  private actualizarEstado(): void {
    const cuentas = this.msal.instance.getAllAccounts();
    this.autenticado = cuentas.length > 0;

    if (this.autenticado && !this.msal.instance.getActiveAccount()) {
      this.msal.instance.setActiveAccount(cuentas[0]);
    }

    this.cdr.markForCheck();
  }

  login(): void {
    this.msal.loginRedirect({
      scopes: ['openid', 'profile', 'email', environment.msal.apiScope]
    });
  }

  logout(): void {
    this.msal.logoutRedirect();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
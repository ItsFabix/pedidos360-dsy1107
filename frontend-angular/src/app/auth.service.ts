import { Injectable, inject } from '@angular/core';
import { MsalService } from '@azure/msal-angular';

export type Rol = 'Admin' | 'Operador' | 'Cliente';

@Injectable({ providedIn: 'root' })
export class AuthService {

    private msal = inject(MsalService);

    cuenta() {
        return this.msal.instance.getAllAccounts()[0] ?? null;
    }

    nombre(): string {
        return this.cuenta()?.name ?? 'Sin sesión';
    }

    correo(): string {
        return this.cuenta()?.username ?? '';
    }

    roles(): Rol[] {
        const claims = this.cuenta()?.idTokenClaims as Record<string, unknown> | undefined;
        const roles = claims?.['roles'];
        return Array.isArray(roles) ? (roles as Rol[]) : [];
    }

    tieneRol(...buscados: Rol[]): boolean {
        return this.roles().some(r => buscados.includes(r));
    }

    esAdmin(): boolean {
        return this.tieneRol('Admin');
    }

    puedeGestionarEstados(): boolean {
        return this.tieneRol('Admin', 'Operador');
    }

    rolPrincipal(): string {
        const roles = this.roles();
        if (roles.includes('Admin')) return 'Admin';
        if (roles.includes('Operador')) return 'Operador';
        if (roles.includes('Cliente')) return 'Cliente';
        return 'Sin rol asignado';
    }
}
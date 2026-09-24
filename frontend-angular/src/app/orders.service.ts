import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { EstadoPedido, Pedido } from './models';

export interface NuevoPedido {
    descripcion?: string;
    items: { productoId: number; cantidad: number }[];
}

@Injectable({ providedIn: 'root' })
export class OrdersService {

    private http = inject(HttpClient);
    private base = `${environment.apiBaseUrl}/api/orders`;

    listar(): Observable<Pedido[]> {
        return this.http.get<Pedido[]>(this.base);
    }

    detalle(id: number): Observable<Pedido> {
        return this.http.get<Pedido>(`${this.base}/${id}`);
    }

    crear(pedido: NuevoPedido): Observable<Pedido> {
        return this.http.post<Pedido>(this.base, pedido);
    }

    cambiarEstado(id: number, estado: EstadoPedido): Observable<Pedido> {
        return this.http.put<Pedido>(`${this.base}/${id}/status`, { estado });
    }
}
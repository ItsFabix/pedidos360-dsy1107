import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';
import { Producto } from './models';

@Injectable({ providedIn: 'root' })
export class CatalogService {

    private http = inject(HttpClient);
    private base = `${environment.apiBaseUrl}/api/catalog/products`;

    listar(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.base);
    }

    crear(producto: Partial<Producto>): Observable<Producto> {
        return this.http.post<Producto>(this.base, producto);
    }

    actualizar(id: number, producto: Partial<Producto>): Observable<Producto> {
        return this.http.put<Producto>(`${this.base}/${id}`, producto);
    }
}
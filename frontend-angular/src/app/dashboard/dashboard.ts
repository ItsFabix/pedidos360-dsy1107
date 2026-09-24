import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../auth.service';
import { CatalogService } from '../catalog.service';
import { OrdersService } from '../orders.service';
import { ETIQUETA_ESTADO, EstadoPedido, Pedido, Producto } from '../models';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.html'
})
export class Dashboard implements OnInit {

    protected auth = inject(AuthService);
    private ordersService = inject(OrdersService);
    private catalogService = inject(CatalogService);
    private cdr = inject(ChangeDetectorRef);

    pedidos: Pedido[] = [];
    productos: Producto[] = [];
    cargando = true;
    error = '';

    etiquetas = ETIQUETA_ESTADO;

    ngOnInit(): void {
        this.ordersService.listar().subscribe({
            next: datos => {
                this.pedidos = datos;
                this.cargando = false;
                this.cdr.detectChanges();
            },
            error: err => {
                this.error = 'No fue posible cargar los pedidos (HTTP ' + err.status + ')';
                this.cargando = false;
                this.cdr.detectChanges();
            }
        });

        this.catalogService.listar().subscribe({
            next: datos => this.productos = datos,
            error: () => { }
        });
    }

    contarPorEstado(estado: EstadoPedido): number {
        return this.pedidos.filter(p => p.estado === estado).length;
    }

    get pendientes(): number {
        return this.pedidos.filter(
            p => p.estado !== 'ENTREGADO' && p.estado !== 'CANCELADO'
        ).length;
    }

    get montoTotal(): number {
        return this.pedidos.reduce((suma, p) => suma + Number(p.total), 0);
    }

    get stockBajo(): Producto[] {
        return this.productos.filter(p => p.stock <= 10);
    }

    get ultimos(): Pedido[] {
        return this.pedidos.slice(0, 5);
    }
}
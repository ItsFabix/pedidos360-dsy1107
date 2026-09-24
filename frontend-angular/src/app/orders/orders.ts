import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../auth.service';
import { CatalogService } from '../catalog.service';
import { OrdersService } from '../orders.service';
import {
    ETIQUETA_ESTADO,
    EstadoPedido,
    Pedido,
    Producto,
    SIGUIENTES_ESTADOS
} from '../models';

interface LineaCarro {
    productoId: number;
    nombre: string;
    precio: number;
    stock: number;
    cantidad: number;
}

@Component({
    selector: 'app-orders',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './orders.html'
})
export class Orders implements OnInit {

    protected auth = inject(AuthService);
    private ordersService = inject(OrdersService);
    private catalogService = inject(CatalogService);
    private cdr = inject(ChangeDetectorRef);

    pedidos: Pedido[] = [];
    productos: Producto[] = [];
    expandido: number | null = null;

    cargando = true;
    error = '';
    exito = '';

    mostrarFormulario = false;
    descripcion = '';
    carro: LineaCarro[] = [];
    productoSeleccionado: number | null = null;
    cantidadSeleccionada = 1;
    guardando = false;

    etiquetas = ETIQUETA_ESTADO;

    ngOnInit(): void {
        this.cargarPedidos();
        this.catalogService.listar().subscribe({
            next: datos => this.productos = datos,
            error: () => { }
        });
    }

    cargarPedidos(): void {
        this.cargando = true;
        this.ordersService.listar().subscribe({
            next: datos => {
                this.pedidos = datos;
                this.cargando = false;
                this.cdr.detectChanges();
            },
            error: err => {
                this.error = this.mensajeError(err);
                this.cargando = false;
                this.cdr.detectChanges();
            }
        });
    }

    alternarDetalle(id: number): void {
        this.expandido = this.expandido === id ? null : id;
    }

    // ----- Carro -----

    agregarAlCarro(): void {
        if (!this.productoSeleccionado || this.cantidadSeleccionada < 1) {
            return;
        }

        const producto = this.productos.find(p => p.id === Number(this.productoSeleccionado));
        if (!producto) return;

        const existente = this.carro.find(l => l.productoId === producto.id);
        if (existente) {
            existente.cantidad += Number(this.cantidadSeleccionada);
        } else {
            this.carro.push({
                productoId: producto.id,
                nombre: producto.nombre,
                precio: Number(producto.precio),
                stock: producto.stock,
                cantidad: Number(this.cantidadSeleccionada)
            });
        }

        this.productoSeleccionado = null;
        this.cantidadSeleccionada = 1;
    }

    quitarDelCarro(productoId: number): void {
        this.carro = this.carro.filter(l => l.productoId !== productoId);
    }

    get totalCarro(): number {
        return this.carro.reduce((s, l) => s + l.precio * l.cantidad, 0);
    }

    crearPedido(): void {
        if (this.carro.length === 0) return;

        this.guardando = true;
        this.error = '';
        this.exito = '';

        this.ordersService.crear({
            descripcion: this.descripcion,
            items: this.carro.map(l => ({ productoId: l.productoId, cantidad: l.cantidad }))
        }).subscribe({
            next: pedido => {
                this.exito = `Pedido N° ${pedido.id} creado correctamente.`;
                this.carro = [];
                this.descripcion = '';
                this.mostrarFormulario = false;
                this.guardando = false;
                this.cargarPedidos();
            },
            error: err => {
                this.error = this.mensajeError(err);
                this.guardando = false;
            }
        });
    }

    // ----- Estados -----

    siguientes(estado: EstadoPedido): EstadoPedido[] {
        return SIGUIENTES_ESTADOS[estado];
    }

    cambiarEstado(pedido: Pedido, estado: EstadoPedido): void {
        this.error = '';
        this.exito = '';

        this.ordersService.cambiarEstado(pedido.id, estado).subscribe({
            next: actualizado => {
                this.exito = `Pedido N° ${actualizado.id} → ${this.etiquetas[actualizado.estado]}.`;
                this.cargarPedidos();
                this.catalogService.listar().subscribe({
                    next: datos => this.productos = datos,
                    error: () => { }
                });
            },
            error: err => this.error = this.mensajeError(err)
        });
    }

    claseEstado(estado: EstadoPedido): string {
        if (estado === 'ENTREGADO') return 'badge verde';
        if (estado === 'CANCELADO') return 'badge rojo';
        if (estado === 'CREADO') return 'badge naranjo';
        return 'badge';
    }

    private mensajeError(err: any): string {
        const detalle = err?.error?.message || err?.error?.error || '';
        if (err?.status === 403) {
            return 'HTTP 403 — No tiene permisos para realizar esta operación.';
        }
        if (err?.status === 409) {
            return `HTTP 409 — ${detalle || 'Operación no permitida en el estado actual del pedido.'}`;
        }
        if (err?.status === 401) {
            return 'HTTP 401 — Sesión no válida o token expirado.';
        }
        return `HTTP ${err?.status ?? '?'} — ${detalle || 'Error inesperado.'}`;
    }
}
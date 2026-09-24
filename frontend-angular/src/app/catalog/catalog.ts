import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../auth.service';
import { CatalogService } from '../catalog.service';
import { Producto } from '../models';

@Component({
    selector: 'app-catalog',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './catalog.html'
})
export class Catalog implements OnInit {

    protected auth = inject(AuthService);
    private catalogService = inject(CatalogService);
    private cdr = inject(ChangeDetectorRef);

    productos: Producto[] = [];
    cargando = true;
    error = '';
    exito = '';

    mostrarFormulario = false;
    editandoId: number | null = null;
    guardando = false;

    form: Partial<Producto> = this.formVacio();

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {
        this.cargando = true;
        this.catalogService.listar().subscribe({
            next: datos => {
                this.productos = datos;
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

    private formVacio(): Partial<Producto> {
        return { nombre: '', descripcion: '', precio: 0, stock: 0 };
    }

    nuevo(): void {
        this.editandoId = null;
        this.form = this.formVacio();
        this.mostrarFormulario = true;
        this.error = '';
        this.exito = '';
    }

    editar(producto: Producto): void {
        this.editandoId = producto.id;
        this.form = { ...producto };
        this.mostrarFormulario = true;
        this.error = '';
        this.exito = '';
    }

    cancelar(): void {
        this.mostrarFormulario = false;
        this.editandoId = null;
        this.form = this.formVacio();
    }

    guardar(): void {
        if (!this.form.nombre) {
            this.error = 'El nombre es obligatorio.';
            return;
        }

        this.guardando = true;
        this.error = '';
        this.exito = '';

        const datos = {
            nombre: this.form.nombre,
            descripcion: this.form.descripcion,
            precio: Number(this.form.precio),
            stock: Number(this.form.stock)
        };

        const peticion = this.editandoId
            ? this.catalogService.actualizar(this.editandoId, datos)
            : this.catalogService.crear(datos);

        peticion.subscribe({
            next: producto => {
                this.exito = this.editandoId
                    ? `Producto "${producto.nombre}" actualizado.`
                    : `Producto "${producto.nombre}" creado con ID ${producto.id}.`;
                this.guardando = false;
                this.cancelar();
                this.cargar();
            },
            error: err => {
                this.error = this.mensajeError(err);
                this.guardando = false;
            }
        });
    }

    claseStock(stock: number): string {
        if (stock === 0) return 'badge rojo';
        if (stock <= 10) return 'badge naranjo';
        return 'badge verde';
    }

    private mensajeError(err: any): string {
        if (err?.status === 403) {
            return 'HTTP 403 — Solo el rol Admin puede modificar el catálogo.';
        }
        if (err?.status === 401) {
            return 'HTTP 401 — Sesión no válida o token expirado.';
        }
        const detalle = err?.error?.message || '';
        return `HTTP ${err?.status ?? '?'} — ${detalle || 'Error inesperado.'}`;
    }
}
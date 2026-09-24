export type EstadoPedido =
    | 'CREADO'
    | 'ACEPTADO'
    | 'EN_PREPARACION'
    | 'DESPACHADO'
    | 'ENTREGADO'
    | 'CANCELADO';

export interface Producto {
    id: number;
    nombre: string;
    descripcion?: string;
    precio: number;
    stock: number;
}

export interface ItemPedido {
    id?: number;
    productoId: number;
    nombreProducto: string;
    cantidad: number;
    precioUnitario: number;
}

export interface Pedido {
    id: number;
    clienteId: string;
    clienteNombre?: string;
    descripcion?: string;
    estado: EstadoPedido;
    fechaCreacion: string;
    total: number;
    items: ItemPedido[];
}

export const ETIQUETA_ESTADO: Record<EstadoPedido, string> = {
    CREADO: 'Creado',
    ACEPTADO: 'Aceptado',
    EN_PREPARACION: 'En preparación',
    DESPACHADO: 'Despachado',
    ENTREGADO: 'Entregado',
    CANCELADO: 'Cancelado'
};

export const SIGUIENTES_ESTADOS: Record<EstadoPedido, EstadoPedido[]> = {
    CREADO: ['ACEPTADO', 'CANCELADO'],
    ACEPTADO: ['EN_PREPARACION', 'CANCELADO'],
    EN_PREPARACION: ['DESPACHADO', 'CANCELADO'],
    DESPACHADO: ['ENTREGADO'],
    ENTREGADO: [],
    CANCELADO: []
};
import { Component } from '@angular/core';

@Component({
  selector: 'app-protegido',
  standalone: true,
  template: `
    <section class="tarjeta protegida">
      <h2>Ruta protegida por MsalGuard</h2>
      <p>Si puedes ver esta sección, MsalGuard permitió el acceso.</p>
      <p>Las llamadas HTTP al API Gateway utilizan MsalInterceptor y el scope <strong>Pedidos.Read</strong>.</p>
    </section>
  `,
  styles: [`
    .tarjeta {
      font-family: Consolas, 'Courier New', monospace;
      background: #fff;
      border: 1px solid #d5dbe3;
      border-left: 5px solid #548235;
      border-radius: 12px;
      padding: 22px;
      margin: 18px 0;
    }
    h2 { color: #e66c00; margin-top: 0; }
  `]
})
export class Protegido {}

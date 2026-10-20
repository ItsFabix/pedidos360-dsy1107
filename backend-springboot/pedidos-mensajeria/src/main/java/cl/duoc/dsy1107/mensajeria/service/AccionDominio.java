package cl.duoc.dsy1107.mensajeria.service;

import cl.duoc.dsy1107.mensajeria.model.PedidoEvento;

@FunctionalInterface
public interface AccionDominio {

    void ejecutar(PedidoEvento evento) throws Exception;
}
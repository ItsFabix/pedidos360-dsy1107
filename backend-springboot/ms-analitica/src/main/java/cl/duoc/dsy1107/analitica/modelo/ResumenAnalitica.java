package cl.duoc.dsy1107.analitica.modelo;

import java.util.Map;

public record ResumenAnalitica(
        long totalEventos,
        long alertasEmitidas,
        Map<String, Long> eventosPorEstado) {
}
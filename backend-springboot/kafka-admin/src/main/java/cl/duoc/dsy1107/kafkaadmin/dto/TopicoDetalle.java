package cl.duoc.dsy1107.kafkaadmin.dto;

import java.util.List;
import java.util.Map;

public record TopicoDetalle(
        String nombre,
        int particiones,
        int replicas,
        Map<String, String> configuracion,
        List<ParticionInfo> detalle) {
}
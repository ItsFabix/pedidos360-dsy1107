package cl.duoc.dsy1107.kafkaadmin.dto;

import java.util.List;

public record GrupoDetalle(
        String grupoId,
        String estado,
        int miembros,
        long lagTotal,
        List<LagParticion> particiones) {
}
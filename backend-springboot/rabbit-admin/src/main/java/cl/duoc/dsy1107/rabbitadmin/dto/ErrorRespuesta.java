package cl.duoc.dsy1107.rabbitadmin.dto;

import java.util.Map;

public record ErrorRespuesta(int estado, String mensaje, Map<String, String> detalles) {
}
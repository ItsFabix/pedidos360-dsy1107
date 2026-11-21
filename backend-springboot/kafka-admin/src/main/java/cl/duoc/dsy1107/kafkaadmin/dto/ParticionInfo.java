package cl.duoc.dsy1107.kafkaadmin.dto;

import java.util.List;

/**
 * Estado de una particion: quien es el lider, donde estan las replicas y cuales
 * estan al dia (ISR).
 */
public record ParticionInfo(int particion, Integer lider, List<Integer> replicas, List<Integer> isr) {
}
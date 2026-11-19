package cl.duoc.dsy1107.pedidosapi.mensajeria;

/**
 * Nombres de los topicos Kafka que usa este microservicio. Se centralizan aqui
 * para no repetir el texto en el codigo. Los topicos se crean con
 * infra/kafka/scripts/03_crear_topicos.ps1 (3 particiones, RF=3).
 */
public final class KafkaTopicos {

    public static final String EVENTOS = "pedidos360.pedidos.eventos";

    private KafkaTopicos() {
    }
}
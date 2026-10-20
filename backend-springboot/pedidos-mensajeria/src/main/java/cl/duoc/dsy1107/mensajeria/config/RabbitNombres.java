package cl.duoc.dsy1107.mensajeria.config;

public final class RabbitNombres {

    private static final String PREFIJO = "pedidos360";

    // ---- Exchanges ----
    public static final String EXCHANGE_DIRECT = PREFIJO + ".direct.exchange";
    public static final String EXCHANGE_TOPIC = PREFIJO + ".topic.exchange";
    public static final String EXCHANGE_RETRY = PREFIJO + ".retry.exchange";
    public static final String EXCHANGE_DLX = PREFIJO + ".dlx";

    // ---- Dominios (también son la routing key del exchange direct) ----
    public static final String DOMINIO_NOTIFICACIONES = "notificaciones";
    public static final String DOMINIO_COCINA = "cocina";
    public static final String DOMINIO_DOCUMENTOS = "documentos";

    // ---- Colas principales ----
    public static final String COLA_NOTIFICACIONES = PREFIJO + "." + DOMINIO_NOTIFICACIONES + ".queue";
    public static final String COLA_COCINA = PREFIJO + "." + DOMINIO_COCINA + ".queue";
    public static final String COLA_DOCUMENTOS = PREFIJO + "." + DOMINIO_DOCUMENTOS + ".queue";

    // ---- Colas de reintento ----
    public static final String RETRY_NOTIFICACIONES = PREFIJO + "." + DOMINIO_NOTIFICACIONES + ".retry";
    public static final String RETRY_COCINA = PREFIJO + "." + DOMINIO_COCINA + ".retry";
    public static final String RETRY_DOCUMENTOS = PREFIJO + "." + DOMINIO_DOCUMENTOS + ".retry";

    // ---- Dead Letter Queues ----
    public static final String DLQ_NOTIFICACIONES = PREFIJO + "." + DOMINIO_NOTIFICACIONES + ".dlq";
    public static final String DLQ_COCINA = PREFIJO + "." + DOMINIO_COCINA + ".dlq";
    public static final String DLQ_DOCUMENTOS = PREFIJO + "." + DOMINIO_DOCUMENTOS + ".dlq";

    // ---- Cola observada por el exchange topic ----
    public static final String COLA_MONITOREO = PREFIJO + ".monitoreo.queue";
    public static final String PATRON_TOPIC_PEDIDO = "pedido.*";

    // ---- Header que cuenta los intentos de un mensaje ----
    public static final String HEADER_INTENTOS = "x-intentos";

    /**
     * Routing key con la que una cola principal envía sus mensajes muertos al DLX.
     */
    public static String claveFallida(String dominio) {
        return dominio + ".failed";
    }

    private RabbitNombres() {
    }
}
package cl.duoc.dsy1107.auditoria.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import cl.duoc.dsy1107.auditoria.modelo.PedidoEvento;

/**
 * Manejo de errores del consumidor.
 *
 * - Reintentos: 3 reintentos, 1 segundo entre cada uno (4 intentos en total).
 * - Agotados los reintentos, el mensaje se publica en "<topico>.DLT" (misma
 * particion).
 * - Un mensaje que no se puede deserializar (no es JSON valido) va directo al
 * DLT,
 * sin reintentos, porque reintentar no lo arregla.
 * - Mientras se reintenta, esa particion espera; el resto de las particiones
 * sigue.
 */
@Configuration
public class KafkaErroresConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErroresConfig.class);

    @Bean
    public DefaultErrorHandler manejadorDeErrores(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        // Productor propio, solo para escribir en el DLT.
        // Un mensaje corrupto se reenvia como bytes; uno valido como JSON.
        Map<Class<?>, Serializer<?>> serializadores = new HashMap<>();
        serializadores.put(byte[].class, new ByteArraySerializer());
        serializadores.put(PedidoEvento.class, new JsonSerializer<PedidoEvento>().noTypeInfo());

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propiedades.put(ProducerConfig.ACKS_CONFIG, "all");
        propiedades.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        propiedades.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

        DefaultKafkaProducerFactory<String, Object> fabrica = new DefaultKafkaProducerFactory<>(
                propiedades, new StringSerializer(), new DelegatingByTypeSerializer(serializadores));
        KafkaTemplate<String, Object> plantillaDlt = new KafkaTemplate<>(fabrica);

        // Destino: "<topico>.DLT", en la MISMA particion que el mensaje original.
        // (Por defecto Spring usaria "<topico>-dlt", que no existe en nuestro cluster.)
        DeadLetterPublishingRecoverer recuperador = new DeadLetterPublishingRecoverer(
                plantillaDlt,
                (registro, error) -> new TopicPartition(registro.topic() + ".DLT", registro.partition()));

        DefaultErrorHandler manejador = new DefaultErrorHandler(recuperador, new FixedBackOff(1000L, 3L));

        // Datos invalidos: no tiene sentido reintentar.
        manejador.addNotRetryableExceptions(IllegalArgumentException.class);

        manejador.setRetryListeners(new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> registro, Exception error, int intento) {
                Throwable causa = (error.getCause() != null) ? error.getCause() : error;
                log.warn("Fallo procesando particion={} offset={} (intento {}): {}",
                        registro.partition(), registro.offset(), intento, causa.getClass().getSimpleName());
            }

            @Override
            public void recovered(ConsumerRecord<?, ?> registro, Exception error) {
                Throwable causa = (error.getCause() != null) ? error.getCause() : error;
                log.error("Mensaje enviado al DLT: topico={}.DLT (origen: particion={} offset={}) causa={}",
                        registro.topic(), registro.partition(), registro.offset(), causa.getClass().getSimpleName());
            }
        });

        return manejador;
    }
}
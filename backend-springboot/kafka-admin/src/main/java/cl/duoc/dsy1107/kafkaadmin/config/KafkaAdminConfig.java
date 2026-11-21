package cl.duoc.dsy1107.kafkaadmin.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente administrador de Kafka: conecta con los 3 brokers. Se cierra al
 * apagar la app.
 */
@Configuration
public class KafkaAdminConfig {

    @Bean(destroyMethod = "close")
    Admin kafkaAdmin(@Value("${kafka.admin.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Si Kafka no responde, que la peticion falle rapido en vez de quedar colgada.
        propiedades.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        propiedades.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 8000);
        return Admin.create(propiedades);
    }
}
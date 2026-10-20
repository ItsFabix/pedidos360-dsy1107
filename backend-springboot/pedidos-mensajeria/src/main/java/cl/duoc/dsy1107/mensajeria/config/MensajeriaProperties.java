package cl.duoc.dsy1107.mensajeria.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pedidos360.mensajeria")
public record MensajeriaProperties(int reintentosMax, long retryTtlMs, List<String> dominiosConFallo) {

    public MensajeriaProperties {
        if (dominiosConFallo == null) {
            dominiosConFallo = List.of();
        }
    }
}
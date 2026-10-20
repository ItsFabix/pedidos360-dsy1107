package cl.duoc.dsy1107.rabbitadmin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.http.HttpStatus;

import cl.duoc.dsy1107.rabbitadmin.dto.ColaInfo;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearBindingRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearColaRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.CrearExchangeRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.TipoExchange;

class RabbitAdminServiceTest {

    private AmqpAdmin admin;
    private RabbitAdminService servicio;

    @BeforeEach
    void preparar() {
        admin = mock(AmqpAdmin.class);
        servicio = new RabbitAdminService(admin);
    }

    private QueueInformation info(String nombre, int mensajes, int consumidores) {
        return new QueueInformation(nombre, mensajes, consumidores);
    }

    private HttpStatus estadoDe(Runnable accion) {
        return assertThrows(ReglaAdminException.class, accion::run).getEstado();
    }

    @Test
    void creaUnaColaConSusArgumentos() {
        when(admin.getQueueInfo("demo.cola")).thenReturn(info("demo.cola", 0, 0));

        ColaInfo resultado = servicio.crearCola(
                new CrearColaRequest("demo.cola", true, 60000L, 100, "pedidos360.dlx", "demo.failed"));

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(admin).declareQueue(captor.capture());
        Queue cola = captor.getValue();
        assertEquals("demo.cola", cola.getName());
        assertEquals(60000L, cola.getArguments().get("x-message-ttl"));
        assertEquals(100, cola.getArguments().get("x-max-length"));
        assertEquals("pedidos360.dlx", cola.getArguments().get("x-dead-letter-exchange"));
        assertEquals("demo.cola", resultado.nombre());
    }

    @Test
    void rechazaNombreDeColaInvalido() {
        assertEquals(HttpStatus.BAD_REQUEST,
                estadoDe(() -> servicio
                        .crearCola(new CrearColaRequest("cola con espacios!", null, null, null, null, null))));
        verify(admin, never()).declareQueue(any(Queue.class));
    }

    @Test
    void rechazaElPrefijoReservadoPorRabbit() {
        assertEquals(HttpStatus.BAD_REQUEST,
                estadoDe(() -> servicio.crearCola(new CrearColaRequest("amq.mi-cola", null, null, null, null, null))));
    }

    @Test
    void noPermiteEliminarLaTopologiaDelSistema() {
        assertEquals(HttpStatus.CONFLICT, estadoDe(() -> servicio.eliminarCola("pedidos360.cocina.queue")));
        assertEquals(HttpStatus.CONFLICT, estadoDe(() -> servicio.eliminarExchange("pedidos360.dlx")));
        verify(admin, never()).deleteQueue(anyString());
        verify(admin, never()).deleteExchange(anyString());
    }

    @Test
    void eliminarUnaColaInexistenteDevuelve404() {
        when(admin.getQueueInfo("no.existe")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, estadoDe(() -> servicio.eliminarCola("no.existe")));
        verify(admin, never()).deleteQueue(anyString());
    }

    @Test
    void eliminaUnaColaExistente() {
        when(admin.getQueueInfo("demo.cola")).thenReturn(info("demo.cola", 0, 0));

        servicio.eliminarCola("demo.cola");

        verify(admin).deleteQueue("demo.cola");
    }

    @Test
    void creaUnExchangeTopic() {
        servicio.crearExchange(new CrearExchangeRequest("demo.topic", TipoExchange.TOPIC, true));

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(admin).declareExchange(captor.capture());
        assertEquals("demo.topic", captor.getValue().getName());
        assertEquals("topic", captor.getValue().getType());
    }

    @Test
    void creaUnBindingSoloSiLaColaExiste() {
        when(admin.getQueueInfo("demo.cola")).thenReturn(info("demo.cola", 0, 0));

        servicio.crearBinding(new CrearBindingRequest("demo.cola", "demo.topic", "demo.*"));

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(admin).declareBinding(captor.capture());
        assertEquals("demo.*", captor.getValue().getRoutingKey());
    }

    @Test
    void crearBindingConColaInexistenteDevuelve404() {
        when(admin.getQueueInfo("fantasma")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND,
                estadoDe(() -> servicio.crearBinding(new CrearBindingRequest("fantasma", "demo.topic", "a.b"))));
        verify(admin, never()).declareBinding(any(Binding.class));
    }

    @Test
    void rechazaRoutingKeyInvalida() {
        assertEquals(HttpStatus.BAD_REQUEST,
                estadoDe(() -> servicio.eliminarBinding("demo.cola", "demo.topic", "clave con espacios")));
    }

    @Test
    void noPermiteEliminarBindingsDelSistema() {
        assertEquals(HttpStatus.CONFLICT,
                estadoDe(() -> servicio.eliminarBinding("pedidos360.cocina.queue", "pedidos360.direct.exchange",
                        "cocina")));
        verify(admin, never()).removeBinding(any(Binding.class));
    }
}
package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.repository.DomicilioRepository;
import com.appetit.gastrosystem.repository.PedidoRepository;
import com.appetit.gastrosystem.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DomicilioService {

    private final DomicilioRepository domicilioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public DomicilioService(DomicilioRepository domicilioRepository,
                            UsuarioRepository usuarioRepository,
                            PedidoRepository pedidoRepository) {
        this.domicilioRepository = domicilioRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Domicilio> listarTodos() {
        return domicilioRepository.findAllByOrderByPedidoFechaPedidoDesc();
    }

    public Optional<Domicilio> buscarPorId(Long id) {
        return domicilioRepository.findById(id);
    }

    public List<Domicilio> listarDisponiblesParaRepartidor() {
        // Pedidos listos para entregar pero sin asignar
        return domicilioRepository.findByEstadoEntrega(EstadoEntrega.PENDIENTE_ASIGNACION);
    }

    public List<Domicilio> listarPorRepartidor(Long idRepartidor) {
        return domicilioRepository.findByRepartidorIdUsuario(idRepartidor);
    }

    public List<Domicilio> listarActivosPorRepartidor(Long idRepartidor) {
        // Domicilios asignados que no están entregados
        return domicilioRepository.findByRepartidorIdUsuarioAndEstadoEntregaIn(idRepartidor,
                Arrays.asList(EstadoEntrega.PENDIENTE_ASIGNACION, EstadoEntrega.EN_CAMINO));
    }

    @Transactional
    public Domicilio asignarRepartidor(Long idDomicilio, Long idRepartidor) {
        Domicilio domicilio = domicilioRepository.findById(idDomicilio)
                .orElseThrow(() -> new IllegalArgumentException("Domicilio no encontrado"));

        Usuario repartidor = usuarioRepository.findById(idRepartidor)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));

        // Validar que el usuario sea repartidor
        boolean esRepartidor = repartidor.getRoles().stream()
                .anyMatch(r -> r.getNombre() == NombreRol.DOMICILIARIO);
        if (!esRepartidor) {
            throw new IllegalArgumentException("El usuario seleccionado no tiene el rol de DOMICILIARIO");
        }

        domicilio.setRepartidor(repartidor);
        domicilio.setEstadoEntrega(EstadoEntrega.EN_CAMINO); // Al asignar, inicia camino
        domicilioRepository.save(domicilio);

        // Actualizar estado del pedido a en_camino? No, el pedido puede pasar a listo o entregado.
        // Vamos a cambiar el estado del pedido a LISTO o ENTREGADO según corresponda.
        // Al asignar al repartidor, el pedido está en camino.
        Pedido pedido = domicilio.getPedido();
        if (pedido.getEstado() == EstadoPedido.LISTO) {
            // El pedido ya está en camino
            // Podemos dejarlo en LISTO o crear una transición intermedia. Pero los requerimientos mínimos piden:
            // recibido, en preparación, listo, entregado, pagado y cancelado.
            // Así que al asignar, sigue LISTO (está en camino). Cuando llegue, se marca como ENTREGADO.
        }

        return domicilio;
    }

    @Transactional
    public Domicilio actualizarEstadoEntrega(Long idDomicilio, EstadoEntrega estadoEntrega) {
        Domicilio domicilio = domicilioRepository.findById(idDomicilio)
                .orElseThrow(() -> new IllegalArgumentException("Domicilio no encontrado"));

        domicilio.setEstadoEntrega(estadoEntrega);
        if (estadoEntrega == EstadoEntrega.ENTREGADO) {
            domicilio.setFechaEntrega(LocalDateTime.now());
            
            // Marcar pedido como ENTREGADO
            Pedido pedido = domicilio.getPedido();
            pedido.setEstado(EstadoPedido.ENTREGADO);
            pedidoRepository.save(pedido);
        }
        
        return domicilioRepository.save(domicilio);
    }
}

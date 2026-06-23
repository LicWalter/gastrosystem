package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PlatoRepository platoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DomicilioRepository domicilioRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         PlatoRepository platoRepository,
                         MesaRepository mesaRepository,
                         UsuarioRepository usuarioRepository,
                         DomicilioRepository domicilioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.platoRepository = platoRepository;
        this.mesaRepository = mesaRepository;
        this.usuarioRepository = usuarioRepository;
        this.domicilioRepository = domicilioRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Optional<Pedido> buscarPorIdConDetalles(Long id) {
        return pedidoRepository.findByIdWithDetalles(id);
    }

    public List<Pedido> listarPorCliente(Long idCliente) {
        return pedidoRepository.findByClienteIdUsuarioOrderByFechaPedidoDesc(idCliente);
    }

    public List<Pedido> listarPorMesero(Long idMesero) {
        return pedidoRepository.findByMeseroIdUsuarioOrderByFechaPedidoDesc(idMesero);
    }

    public List<Pedido> listarPedidosCocina() {
        // Cocina ve pedidos RECIBIDO o EN_PREPARACION
        return pedidoRepository.findByEstadoIn(Arrays.asList(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION));
    }

    public List<Pedido> listarPedidosActivos() {
        // Todos menos pagados y cancelados
        return pedidoRepository.findByEstadoIn(Arrays.asList(
                EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO, EstadoPedido.ENTREGADO
        ));
    }

    public List<Pedido> listarPorTipoYEstados(TipoPedido tipo, List<EstadoPedido> estados) {
        return pedidoRepository.findByTipoPedidoAndEstadoIn(tipo, estados);
    }

    public Pedido obtenerPedidoActivoMesa(Integer idMesa) {
        List<Pedido> activos = pedidoRepository.findByMesaIdMesaAndEstadoNotIn(idMesa, 
                Arrays.asList(EstadoPedido.PAGADO, EstadoPedido.CANCELADO));
        return activos.isEmpty() ? null : activos.get(0);
    }

    @Transactional
    public Pedido crearPedidoMesa(Integer idMesa, Long idMesero) {
        Mesa mesa = mesaRepository.findById(idMesa)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));
        
        // Verificar si la mesa ya tiene un pedido activo (no pagado ni cancelado)
        List<Pedido> activos = pedidoRepository.findByMesaIdMesaAndEstadoNotIn(idMesa, 
                Arrays.asList(EstadoPedido.PAGADO, EstadoPedido.CANCELADO));
        if (!activos.isEmpty()) {
            return activos.get(0); // Devolver el pedido activo existente
        }

        Usuario mesero = usuarioRepository.findById(idMesero)
                .orElseThrow(() -> new IllegalArgumentException("Mesero no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMesero(mesero);
        pedido.setTipoPedido(TipoPedido.MESA);
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setTotal(BigDecimal.ZERO);

        mesa.setEstado(EstadoMesa.OCUPADA);
        mesaRepository.save(mesa);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido crearPedidoDomicilio(Long idCliente, String direccionEntrega) {
        Usuario cliente = usuarioRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setTipoPedido(TipoPedido.DOMICILIO);
        pedido.setEstado(EstadoPedido.RECIBIDO);
        pedido.setTotal(BigDecimal.ZERO);

        pedido = pedidoRepository.save(pedido);

        // Crear registro de Domicilio asociado
        Domicilio domicilio = new Domicilio();
        domicilio.setPedido(pedido);
        domicilio.setDireccionEntrega(direccionEntrega);
        domicilio.setEstadoEntrega(EstadoEntrega.PENDIENTE_ASIGNACION);
        domicilioRepository.save(domicilio);

        pedido.setDomicilio(domicilio);
        return pedido;
    }

    @Transactional
    public Pedido agregarItem(Long idPedido, Long idPlato, Integer cantidad) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.PAGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se pueden agregar ítems a un pedido pagado o cancelado");
        }

        Plato plato = platoRepository.findById(idPlato)
                .orElseThrow(() -> new IllegalArgumentException("Plato no encontrado"));

        if (!plato.getActivo()) {
            throw new IllegalArgumentException("El plato seleccionado no está activo en el menú");
        }

        // Buscar si ya existe el plato en los detalles del pedido
        Optional<DetallePedido> existente = pedido.getDetalles().stream()
                .filter(d -> d.getPlato().getIdPlato().equals(idPlato))
                .findFirst();

        if (existente.isPresent()) {
            DetallePedido detalle = existente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalle.recalcularSubtotal();
            detallePedidoRepository.save(detalle);
        } else {
            DetallePedido nuevoDetalle = new DetallePedido(pedido, plato, cantidad);
            pedido.agregarDetalle(nuevoDetalle);
            detallePedidoRepository.save(nuevoDetalle);
        }

        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido removerItem(Long idPedido, Long idDetalle) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.PAGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se pueden remover ítems de un pedido pagado o cancelado");
        }

        DetallePedido detalle = detallePedidoRepository.findById(idDetalle)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado"));

        pedido.removerDetalle(detalle);
        detallePedidoRepository.delete(detalle);

        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);

        // Lógica de mesa física al cambiar estados
        if (pedido.getTipoPedido() == TipoPedido.MESA && pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            if (nuevoEstado == EstadoPedido.PAGADO || nuevoEstado == EstadoPedido.CANCELADO) {
                // Liberar la mesa
                mesa.setEstado(EstadoMesa.DISPONIBLE);
                mesaRepository.save(mesa);
            } else {
                mesa.setEstado(EstadoMesa.OCUPADA);
                mesaRepository.save(mesa);
            }
        }

        // Lógica de Domicilio al cambiar estados
        if (pedido.getTipoPedido() == TipoPedido.DOMICILIO && pedido.getDomicilio() != null) {
            Domicilio domicilio = pedido.getDomicilio();
            if (nuevoEstado == EstadoPedido.ENTREGADO) {
                domicilio.setEstadoEntrega(EstadoEntrega.ENTREGADO);
                domicilio.setFechaEntrega(LocalDateTime.now());
                domicilioRepository.save(domicilio);
            } else if (nuevoEstado == EstadoPedido.CANCELADO) {
                // Si se cancela el pedido, el domicilio también se ve afectado
                domicilio.setEstadoEntrega(EstadoEntrega.ENTREGADO); // Finalizar de algún modo
                domicilioRepository.save(domicilio);
            }
        }

        return pedidoRepository.save(pedido);
    }
}

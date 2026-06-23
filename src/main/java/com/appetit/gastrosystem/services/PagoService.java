package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.EstadoPedido;
import com.appetit.gastrosystem.model.MetodoPago;
import com.appetit.gastrosystem.model.Pago;
import com.appetit.gastrosystem.model.Pedido;
import com.appetit.gastrosystem.repository.PagoRepository;
import com.appetit.gastrosystem.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    public PagoService(PagoRepository pagoRepository,
                       PedidoRepository pedidoRepository,
                       PedidoService pedidoService) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoService = pedidoService;
    }

    @Transactional
    public Pago registrarPago(Long idPedido, MetodoPago metodoPago) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalStateException("El pedido ya se encuentra pagado");
        }

        if (pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No se puede registrar un pago de un pedido con total en cero o menor");
        }

        // Crear pago
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(pedido.getTotal());
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        // Cambiar estado a PAGADO
        pedidoService.cambiarEstado(idPedido, EstadoPedido.PAGADO);

        return pago;
    }

    public Optional<Pago> buscarPorPedido(Long idPedido) {
        // Pedido has OneToOne relation with Pago, so it can be traversed directly
        return pedidoRepository.findById(idPedido).map(Pedido::getPago);
    }
}

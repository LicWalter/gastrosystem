package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.EstadoPedido;
import com.appetit.gastrosystem.model.Pedido;
import com.appetit.gastrosystem.model.TipoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteIdUsuarioOrderByFechaPedidoDesc(Long idCliente);
    List<Pedido> findByMeseroIdUsuarioOrderByFechaPedidoDesc(Long idMesero);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByEstadoIn(Collection<EstadoPedido> estados);
    List<Pedido> findByTipoPedidoAndEstadoIn(TipoPedido tipoPedido, Collection<EstadoPedido> estados);
    List<Pedido> findByMesaIdMesaAndEstadoNotIn(Integer idMesa, Collection<EstadoPedido> estados);
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    List<Pedido> findByFechaPedidoBetween(LocalDateTime start, LocalDateTime end);

    // Fetch join para cargar pedido con todos sus detalles (evita LazyInitializationException)
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.cliente " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.plato pl " +
           "LEFT JOIN FETCH pl.categoria " +
           "LEFT JOIN FETCH p.domicilio " +
           "LEFT JOIN FETCH p.pago " +
           "WHERE p.idPedido = :id")
    Optional<Pedido> findByIdWithDetalles(Long id);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.cliente " +
           "LEFT JOIN FETCH p.mesero " +
           "LEFT JOIN FETCH p.domicilio " +
           "LEFT JOIN FETCH p.pago " +
           "WHERE p.fechaPedido BETWEEN :start AND :end " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosDelDiaConRelaciones(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = com.appetit.gastrosystem.model.EstadoPedido.PAGADO AND p.fechaPedido BETWEEN :start AND :end")
    Double sumTotalVentas(LocalDateTime start, LocalDateTime end);

    @Query("SELECT dp.plato.nombre, SUM(dp.cantidad) FROM DetallePedido dp JOIN dp.pedido p " +
           "WHERE p.estado <> com.appetit.gastrosystem.model.EstadoPedido.CANCELADO AND p.fechaPedido BETWEEN :start AND :end " +
           "GROUP BY dp.plato.nombre ORDER BY SUM(dp.cantidad) DESC")
    List<Object[]> findPlatosMasVendidos(LocalDateTime start, LocalDateTime end);
}

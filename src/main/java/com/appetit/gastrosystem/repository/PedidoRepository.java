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

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado = 'PAGADO' AND p.fechaPedido BETWEEN :start AND :end")
    Double sumTotalVentas(LocalDateTime start, LocalDateTime end);

    @Query("SELECT dp.plato.nombre, SUM(dp.cantidad) FROM DetallePedido dp JOIN dp.pedido p " +
           "WHERE p.estado = 'PAGADO' AND p.fechaPedido BETWEEN :start AND :end " +
           "GROUP BY dp.plato.nombre ORDER BY SUM(dp.cantidad) DESC")
    List<Object[]> findPlatosMasVendidos(LocalDateTime start, LocalDateTime end);
}

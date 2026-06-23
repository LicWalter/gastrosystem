package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.Domicilio;
import com.appetit.gastrosystem.model.EstadoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {
    List<Domicilio> findByEstadoEntrega(EstadoEntrega estado);
    List<Domicilio> findByEstadoEntregaIn(Collection<EstadoEntrega> estados);
    List<Domicilio> findByRepartidorIdUsuario(Long idRepartidor);
    List<Domicilio> findByRepartidorIdUsuarioAndEstadoEntregaIn(Long idRepartidor, Collection<EstadoEntrega> estados);
    List<Domicilio> findAllByOrderByPedidoFechaPedidoDesc();
}

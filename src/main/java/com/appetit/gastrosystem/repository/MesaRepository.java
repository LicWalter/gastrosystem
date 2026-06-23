package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.EstadoMesa;
import com.appetit.gastrosystem.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {
    List<Mesa> findByEstado(EstadoMesa estado);
    Optional<Mesa> findByNumero(Integer numero);
    List<Mesa> findAllByOrderByNumeroAsc();
}

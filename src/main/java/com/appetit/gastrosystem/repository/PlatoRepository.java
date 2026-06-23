package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {
    List<Plato> findByCategoriaIdCategoria(Integer idCategoria);
    List<Plato> findByActivoTrue();
    List<Plato> findByActivoTrueAndCategoriaIdCategoria(Integer idCategoria);
}

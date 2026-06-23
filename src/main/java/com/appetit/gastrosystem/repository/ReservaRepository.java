package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioIdUsuarioOrderByFechaHoraDesc(Long idUsuario);
    List<Reserva> findAllByOrderByFechaHoraDesc();
}

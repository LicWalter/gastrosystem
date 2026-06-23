package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByFechaPagoBetween(LocalDateTime start, LocalDateTime end);
}

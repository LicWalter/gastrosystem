package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;

    public ReporteService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Double obtenerVentasDelDia() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        Double sum = pedidoRepository.sumTotalVentas(start, end);
        return sum != null ? sum : 0.0;
    }

    public Map<String, Long> obtenerPlatosMasVendidosDelDia() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        List<Object[]> raw = pedidoRepository.findPlatosMasVendidos(start, end);
        
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            if (row.length >= 2) {
                String plato = (String) row[0];
                Long cantidad = (Long) row[1];
                result.put(plato, cantidad);
            }
        }
        return result;
    }
}

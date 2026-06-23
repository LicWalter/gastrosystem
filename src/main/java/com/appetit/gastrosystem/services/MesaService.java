package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.EstadoMesa;
import com.appetit.gastrosystem.model.Mesa;
import com.appetit.gastrosystem.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public List<Mesa> listarTodas() {
        return mesaRepository.findAllByOrderByNumeroAsc();
    }

    public List<Mesa> listarPorEstado(EstadoMesa estado) {
        return mesaRepository.findByEstado(estado);
    }

    public Optional<Mesa> buscarPorId(Integer id) {
        return mesaRepository.findById(id);
    }

    public Optional<Mesa> buscarPorNumero(Integer numero) {
        return mesaRepository.findByNumero(numero);
    }

    @Transactional
    public Mesa guardarMesa(Mesa mesa) {
        if (mesa.getIdMesa() == null) {
            // Validar número único
            if (mesaRepository.findByNumero(mesa.getNumero()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una mesa con el número " + mesa.getNumero());
            }
        }
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa actualizarEstadoMesa(Integer id, EstadoMesa estado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));
        mesa.setEstado(estado);
        return mesaRepository.save(mesa);
    }

    @Transactional
    public void eliminarMesa(Integer id) {
        mesaRepository.deleteById(id);
    }
}

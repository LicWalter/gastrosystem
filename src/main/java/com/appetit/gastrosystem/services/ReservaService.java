package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.EstadoMesa;
import com.appetit.gastrosystem.model.EstadoReserva;
import com.appetit.gastrosystem.model.Mesa;
import com.appetit.gastrosystem.model.Reserva;
import com.appetit.gastrosystem.model.Usuario;
import com.appetit.gastrosystem.repository.MesaRepository;
import com.appetit.gastrosystem.repository.ReservaRepository;
import com.appetit.gastrosystem.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          MesaRepository mesaRepository,
                          UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.mesaRepository = mesaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Reserva> listarPorCliente(Long idCliente) {
        return reservaRepository.findByUsuarioIdUsuarioOrderByFechaHoraDesc(idCliente);
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    @Transactional
    public Reserva crearReserva(Reserva reserva) {
        if (reserva.getUsuario() != null && reserva.getUsuario().getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(reserva.getUsuario().getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            reserva.setUsuario(usuario);
        }
        reserva.setEstado(EstadoReserva.PENDIENTE);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cambiarEstadoReserva(Long id, EstadoReserva estado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        reserva.setEstado(estado);

        // Si se confirma y tiene mesa, se podría marcar la mesa como RESERVADA
        if (estado == EstadoReserva.CONFIRMADA && reserva.getMesa() != null) {
            Mesa mesa = reserva.getMesa();
            mesa.setEstado(EstadoMesa.RESERVADA);
            mesaRepository.save(mesa);
        } else if (estado == EstadoReserva.CANCELADA && reserva.getMesa() != null) {
            // Liberar la mesa si se cancela la reserva
            Mesa mesa = reserva.getMesa();
            if (mesa.getEstado() == EstadoMesa.RESERVADA) {
                mesa.setEstado(EstadoMesa.DISPONIBLE);
                mesaRepository.save(mesa);
            }
        }
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva asignarMesa(Long idReserva, Integer idMesa) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        Mesa mesa = null;
        if (idMesa != null) {
            mesa = mesaRepository.findById(idMesa)
                    .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));
            
            // Asignar y marcar mesa como reservada
            reserva.setMesa(mesa);
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                mesa.setEstado(EstadoMesa.RESERVADA);
                mesaRepository.save(mesa);
            }
        } else {
            // Desasignar
            Mesa mesaAnterior = reserva.getMesa();
            if (mesaAnterior != null && mesaAnterior.getEstado() == EstadoMesa.RESERVADA) {
                mesaAnterior.setEstado(EstadoMesa.DISPONIBLE);
                mesaRepository.save(mesaAnterior);
            }
            reserva.setMesa(null);
        }
        
        return reservaRepository.save(reserva);
    }
}

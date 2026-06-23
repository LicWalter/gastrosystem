package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa")
    private Mesa mesa;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "numero_personas", nullable = false)
    private Integer numeroPersonas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    public Reserva() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.estado == null) {
            this.estado = EstadoReserva.PENDIENTE;
        }
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(Integer numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva{idReserva=" + idReserva + ", fechaHora=" + fechaHora + ", estado=" + estado + "}";
    }
}

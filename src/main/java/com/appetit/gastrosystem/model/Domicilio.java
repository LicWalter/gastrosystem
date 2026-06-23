package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "domicilios")
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_domicilio")
    private Long idDomicilio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repartidor")
    private Usuario repartidor;

    @Column(name = "direccion_entrega", nullable = false, length = 255)
    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false, length = 30)
    private EstadoEntrega estadoEntrega;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    public Domicilio() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.estadoEntrega == null) {
            this.estadoEntrega = EstadoEntrega.PENDIENTE_ASIGNACION;
        }
    }

    public Long getIdDomicilio() {
        return idDomicilio;
    }

    public void setIdDomicilio(Long idDomicilio) {
        this.idDomicilio = idDomicilio;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Usuario getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(Usuario repartidor) {
        this.repartidor = repartidor;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public EstadoEntrega getEstadoEntrega() {
        return estadoEntrega;
    }

    public void setEstadoEntrega(EstadoEntrega estadoEntrega) {
        this.estadoEntrega = estadoEntrega;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    @Override
    public String toString() {
        return "Domicilio{idDomicilio=" + idDomicilio + ", estadoEntrega=" + estadoEntrega + "}";
    }
}

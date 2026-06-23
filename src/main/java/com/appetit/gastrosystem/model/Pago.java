package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    public Pago() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.fechaPago == null) {
            this.fechaPago = LocalDateTime.now();
        }
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    @Override
    public String toString() {
        return "Pago{idPago=" + idPago + ", metodoPago=" + metodoPago + ", monto=" + monto + "}";
    }
}

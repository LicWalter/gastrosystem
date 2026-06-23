package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plato", nullable = false)
    private Plato plato;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public DetallePedido() {
    }

    public DetallePedido(Pedido pedido, Plato plato, Integer cantidad) {
        this.pedido = pedido;
        this.plato = plato;
        this.cantidad = cantidad;
        this.precioUnitario = plato.getPrecio();
        recalcularSubtotal();
    }

    public void recalcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
        if (plato != null) {
            this.precioUnitario = plato.getPrecio();
        }
        recalcularSubtotal();
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "DetallePedido{idDetalle=" + idDetalle + ", plato=" + (plato != null ? plato.getNombre() : "null") + ", cantidad=" + cantidad + ", subtotal=" + subtotal + "}";
    }
}

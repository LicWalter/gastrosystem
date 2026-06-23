package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesero")
    private Usuario mesero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa")
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pedido", nullable = false, length = 20)
    private TipoPedido tipoPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Domicilio domicilio;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    public Pedido() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.fechaPedido == null) {
            this.fechaPedido = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPedido.RECIBIDO;
        }
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
    }

    public void recalcularTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (DetallePedido detalle : detalles) {
            if (detalle.getSubtotal() != null) {
                sum = sum.add(detalle.getSubtotal());
            }
        }
        this.total = sum;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
        recalcularTotal();
    }

    public void removerDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
        recalcularTotal();
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Usuario getMesero() {
        return mesero;
    }

    public void setMesero(Usuario mesero) {
        this.mesero = mesero;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public TipoPedido getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(TipoPedido tipoPedido) {
        this.tipoPedido = tipoPedido;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
        recalcularTotal();
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    @Override
    public String toString() {
        return "Pedido{idPedido=" + idPedido + ", tipoPedido=" + tipoPedido + ", estado=" + estado + ", total=" + total + "}";
    }
}

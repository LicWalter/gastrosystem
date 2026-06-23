package com.appetit.gastrosystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    private Integer idMesa;

    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoMesa estado;

    public Mesa() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.estado == null) {
            this.estado = EstadoMesa.DISPONIBLE;
        }
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Integer idMesa) {
        this.idMesa = idMesa;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Mesa{idMesa=" + idMesa + ", numero=" + numero + ", estado=" + estado + "}";
    }
}

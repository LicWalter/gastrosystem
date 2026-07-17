package com.appetit.gastrosystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "platos")
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plato")
    private Long idPlato;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "imagen", length = 255)
    private String imagen;

    public Plato() {
    }

    @PrePersist
    protected void alCrear() {
        if (this.activo == null) {
            this.activo = true;
        }
    }

    public Long getIdPlato() {
        return idPlato;
    }

    public void setIdPlato(Long idPlato) {
        this.idPlato = idPlato;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagenUrl() {
        if (this.imagen != null && !this.imagen.trim().isEmpty()) {
            return "/img/" + this.imagen;
        }
        
        String nombreNormalizado = this.nombre != null ? this.nombre.toLowerCase().replaceAll("[^a-z0-9]", "") : "";
        if (nombreNormalizado.contains("hamburguesa")) {
            return "/img/hamburguesa.png";
        } else if (nombreNormalizado.contains("alita")) {
            return "/img/alitas.jpeg";
        } else if (nombreNormalizado.contains("brownie")) {
            return "/img/brownieconhelado.jpg";
        } else if (nombreNormalizado.contains("chaparrita")) {
            return "/img/chaparritas.jfif";
        } else if (nombreNormalizado.contains("vino")) {
            return "/img/copavino.jpg";
        } else if (nombreNormalizado.contains("dedo")) {
            return "/img/dedosdequeso.jpg";
        } else if (nombreNormalizado.contains("flan")) {
            return "/img/flan.jpg";
        } else if (nombreNormalizado.contains("gaseosa")) {
            return "/img/gaseosaPersonal.jpg";
        } else if (nombreNormalizado.contains("limonada")) {
            return "/img/limonada.jpg";
        } else if (nombreNormalizado.contains("nacho")) {
            return "/img/nachos.webp";
        } else if (nombreNormalizado.contains("pollo")) {
            return "/img/polloensalsa.jpg";
        } else if (nombreNormalizado.contains("agua")) {
            return "/img/Aguamineral.jpg";
        }
        
        if (this.categoria != null) {
            String catLower = this.categoria.getNombre().toLowerCase();
            if (catLower.contains("bebida")) {
                return "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500&auto=format&fit=crop";
            } else if (catLower.contains("postre")) {
                return "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500&auto=format&fit=crop";
            } else if (catLower.contains("entrada")) {
                return "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?w=500&auto=format&fit=crop";
            }
        }
        return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop";
    }

    @Override
    public String toString() {
        return "Plato{idPlato=" + idPlato + ", nombre='" + nombre + "', precio=" + precio + "}";
    }
}

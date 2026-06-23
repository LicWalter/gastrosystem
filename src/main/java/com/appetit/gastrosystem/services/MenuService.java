package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.Categoria;
import com.appetit.gastrosystem.model.Plato;
import com.appetit.gastrosystem.repository.CategoriaRepository;
import com.appetit.gastrosystem.repository.PlatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final CategoriaRepository categoriaRepository;
    private final PlatoRepository platoRepository;

    public MenuService(CategoriaRepository categoriaRepository, PlatoRepository platoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.platoRepository = platoRepository;
    }

    // --- Categoria CRUD ---
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminarCategoria(Integer id) {
        categoriaRepository.deleteById(id);
    }

    // --- Plato CRUD ---
    public List<Plato> listarPlatos() {
        return platoRepository.findAll();
    }

    public List<Plato> listarPlatosActivos() {
        return platoRepository.findByActivoTrue();
    }

    public List<Plato> listarPlatosPorCategoria(Integer idCategoria) {
        return platoRepository.findByCategoriaIdCategoria(idCategoria);
    }

    public List<Plato> listarPlatosActivosPorCategoria(Integer idCategoria) {
        return platoRepository.findByActivoTrueAndCategoriaIdCategoria(idCategoria);
    }

    public Optional<Plato> buscarPlatoPorId(Long id) {
        return platoRepository.findById(id);
    }

    @Transactional
    public Plato guardarPlato(Plato plato) {
        return platoRepository.save(plato);
    }

    @Transactional
    public Plato actualizarEstadoPlato(Long id, boolean activo) {
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plato no encontrado"));
        plato.setActivo(activo);
        return platoRepository.save(plato);
    }

    @Transactional
    public void eliminarPlato(Long id) {
        platoRepository.deleteById(id);
    }
}

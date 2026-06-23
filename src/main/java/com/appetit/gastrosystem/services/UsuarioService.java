package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.NombreRol;
import com.appetit.gastrosystem.model.Rol;
import com.appetit.gastrosystem.model.Usuario;
import com.appetit.gastrosystem.repository.RolRepository;
import com.appetit.gastrosystem.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarCliente(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);

        Rol rolCliente = rolRepository.findByNombre(NombreRol.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("El rol CLIENTE no existe en la base de datos"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarUsuarioInterno(Usuario usuario, NombreRol nombreRol) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(true);

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("El rol " + nombreRol + " no existe"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(NombreRol nombreRol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre() == nombreRol))
                .collect(Collectors.toList());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public Usuario actualizarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}

package com.appetit.gastrosystem.repository;

import com.appetit.gastrosystem.model.NombreRol;
import com.appetit.gastrosystem.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(NombreRol nombre);
}

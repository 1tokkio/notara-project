package com.notara.usuarios.repositories;

import com.notara.usuarios.models.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Long> {
    Optional<Progreso> findByUsuarioEmail(String usuarioEmail);
}

package cl.notara.ms_pagos_subscripciones.repositories;

import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List<Suscripcion> findByIdUsuario(Long idUsuario);

    List<Suscripcion> findByEstado(EstadoSuscripcion estado);

    Optional<Suscripcion> findByIdUsuarioAndEstado(Long idUsuario, EstadoSuscripcion estado);
}

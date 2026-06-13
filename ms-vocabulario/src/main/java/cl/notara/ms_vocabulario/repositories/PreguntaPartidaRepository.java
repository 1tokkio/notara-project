package cl.notara.ms_vocabulario.repositories;

import cl.notara.ms_vocabulario.models.EstadoPregunta;
import cl.notara.ms_vocabulario.models.PreguntaPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreguntaPartidaRepository extends JpaRepository<PreguntaPartida, Long> {

    List<PreguntaPartida> findByPartidaIdOrderByOrden(Long partidaId);

    Optional<PreguntaPartida> findByPartidaIdAndOrden(Long partidaId, int orden);

    Optional<PreguntaPartida> findByPartidaIdAndEstado(Long partidaId, EstadoPregunta estado);
}

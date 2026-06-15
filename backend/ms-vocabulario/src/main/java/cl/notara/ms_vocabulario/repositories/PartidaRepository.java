package cl.notara.ms_vocabulario.repositories;

import cl.notara.ms_vocabulario.models.EstadoPartida;
import cl.notara.ms_vocabulario.models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByIdUsuarioOrderByFechaInicioDesc(Long idUsuario);

    List<Partida> findByIdUsuarioAndEstado(Long idUsuario, EstadoPartida estado);
}

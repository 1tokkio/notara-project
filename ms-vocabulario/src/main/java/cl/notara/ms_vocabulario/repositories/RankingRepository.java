package cl.notara.ms_vocabulario.repositories;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    List<Ranking> findTop10ByCategoriaOrderByMejorPuntuacionDesc(Categoria categoria);

    List<Ranking> findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc();

    Optional<Ranking> findByIdUsuarioAndCategoria(Long idUsuario, Categoria categoria);

    Optional<Ranking> findByIdUsuarioAndCategoriaIsNull(Long idUsuario);

    List<Ranking> findByIdUsuario(Long idUsuario);
}

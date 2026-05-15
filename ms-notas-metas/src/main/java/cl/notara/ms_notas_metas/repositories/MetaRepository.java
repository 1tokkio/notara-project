package cl.notara.ms_notas_metas.repositories;

import cl.notara.ms_notas_metas.models.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByIdUsuario(Long idUsuario);
}

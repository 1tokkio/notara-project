package cl.notara.ms_notas_metas.repositories;

import cl.notara.ms_notas_metas.models.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {
}

package cl.notara.ms_vocabulario.repositories;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Palabra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PalabraRepository extends JpaRepository<Palabra, Long> {

    List<Palabra> findByCategoriaAndActivaTrue(Categoria categoria);

    long countByCategoriaAndActivaTrue(Categoria categoria);

    @Query("SELECT p FROM Palabra p WHERE p.categoria = :categoria AND p.activa = true ORDER BY FUNCTION('RANDOM')")
    List<Palabra> findRandomByCategoria(Categoria categoria, org.springframework.data.domain.Pageable pageable);

    boolean existsByPalabraIgnoreCaseAndCategoria(String palabra, Categoria categoria);
}

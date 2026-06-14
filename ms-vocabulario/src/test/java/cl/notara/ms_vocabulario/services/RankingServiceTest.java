package cl.notara.ms_vocabulario.services;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Partida;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.repositories.RankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RankingRepository rankingRepo;

    @InjectMocks
    private RankingService rankingService;

    private Partida partida;

    @BeforeEach
    void setUp() {
        partida = new Partida();
        partida.setId(1L);
        partida.setIdUsuario(10L);
        partida.setNombreUsuario("Jugador Test");
        partida.setCategoria(Categoria.BASICO);
        partida.setPuntuacion(150);
        partida.setPalabrasCorrectas(4);
        partida.setTotalPreguntas(5);
        partida.setMejorRacha(3);
    }

    // ─── actualizarRanking() ──────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarRanking() - sin ranking previo → crea dos nuevas entradas (categoría + global)")
    void actualizarRanking_sinRankingPrevio_creaDosEntradas() {
        when(rankingRepo.findByIdUsuarioAndCategoria(10L, Categoria.BASICO)).thenReturn(Optional.empty());
        when(rankingRepo.findByIdUsuarioAndCategoriaIsNull(10L)).thenReturn(Optional.empty());
        when(rankingRepo.save(any(Ranking.class))).thenAnswer(inv -> inv.getArgument(0));

        rankingService.actualizarRanking(partida);

        verify(rankingRepo, times(2)).save(any(Ranking.class));
    }

    @Test
    @DisplayName("actualizarRanking() - con ranking existente → actualiza estadísticas acumuladas")
    void actualizarRanking_rankingExistente_actualizaAcumulados() {
        Ranking rankingExistente = new Ranking();
        rankingExistente.setIdUsuario(10L);
        rankingExistente.setNombreUsuario("Jugador Test");
        rankingExistente.setCategoria(Categoria.BASICO);
        rankingExistente.setMejorPuntuacion(100);
        rankingExistente.setPuntuacionTotal(100);
        rankingExistente.setTotalPartidas(1);
        rankingExistente.setTotalPalabrasCorrectas(3);
        rankingExistente.setTotalPalabras(5);
        rankingExistente.setMejorRacha(2);

        Ranking rankingGlobal = new Ranking();
        rankingGlobal.setIdUsuario(10L);
        rankingGlobal.setNombreUsuario("Jugador Test");
        rankingGlobal.setMejorPuntuacion(100);
        rankingGlobal.setPuntuacionTotal(100);
        rankingGlobal.setTotalPartidas(1);

        when(rankingRepo.findByIdUsuarioAndCategoria(10L, Categoria.BASICO))
                .thenReturn(Optional.of(rankingExistente));
        when(rankingRepo.findByIdUsuarioAndCategoriaIsNull(10L))
                .thenReturn(Optional.of(rankingGlobal));
        when(rankingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        rankingService.actualizarRanking(partida);

        ArgumentCaptor<Ranking> captor = ArgumentCaptor.forClass(Ranking.class);
        verify(rankingRepo, times(2)).save(captor.capture());

        Ranking rankingCategoria = captor.getAllValues().stream()
                .filter(r -> r.getCategoria() == Categoria.BASICO)
                .findFirst().orElseThrow();

        assertThat(rankingCategoria.getTotalPartidas()).isEqualTo(2);
        assertThat(rankingCategoria.getPuntuacionTotal()).isEqualTo(250L);
        assertThat(rankingCategoria.getMejorPuntuacion()).isEqualTo(150);
        assertThat(rankingCategoria.getTotalPalabrasCorrectas()).isEqualTo(7);
        assertThat(rankingCategoria.getTotalPalabras()).isEqualTo(10);
    }

    @Test
    @DisplayName("actualizarRanking() - nueva puntuación mayor → actualiza mejor puntuación")
    void actualizarRanking_nuevaMejorPuntuacion_actualiza() {
        Ranking rankingExistente = new Ranking();
        rankingExistente.setIdUsuario(10L);
        rankingExistente.setCategoria(Categoria.BASICO);
        rankingExistente.setMejorPuntuacion(100);
        rankingExistente.setMejorRacha(1);

        Ranking rankingGlobal = new Ranking();
        rankingGlobal.setIdUsuario(10L);
        rankingGlobal.setMejorPuntuacion(100);
        rankingGlobal.setMejorRacha(1);

        when(rankingRepo.findByIdUsuarioAndCategoria(10L, Categoria.BASICO))
                .thenReturn(Optional.of(rankingExistente));
        when(rankingRepo.findByIdUsuarioAndCategoriaIsNull(10L))
                .thenReturn(Optional.of(rankingGlobal));
        when(rankingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        rankingService.actualizarRanking(partida);

        assertThat(rankingExistente.getMejorPuntuacion()).isEqualTo(150);
        assertThat(rankingExistente.getMejorRacha()).isEqualTo(3);
    }

    @Test
    @DisplayName("actualizarRanking() - puntuación menor → no actualiza mejor puntuación")
    void actualizarRanking_puntuacionMenor_noActualizaMejor() {
        partida.setPuntuacion(50);
        partida.setMejorRacha(1);

        Ranking rankingExistente = new Ranking();
        rankingExistente.setIdUsuario(10L);
        rankingExistente.setCategoria(Categoria.BASICO);
        rankingExistente.setMejorPuntuacion(200);
        rankingExistente.setMejorRacha(5);

        Ranking rankingGlobal = new Ranking();
        rankingGlobal.setIdUsuario(10L);
        rankingGlobal.setMejorPuntuacion(200);
        rankingGlobal.setMejorRacha(5);

        when(rankingRepo.findByIdUsuarioAndCategoria(10L, Categoria.BASICO))
                .thenReturn(Optional.of(rankingExistente));
        when(rankingRepo.findByIdUsuarioAndCategoriaIsNull(10L))
                .thenReturn(Optional.of(rankingGlobal));
        when(rankingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        rankingService.actualizarRanking(partida);

        assertThat(rankingExistente.getMejorPuntuacion()).isEqualTo(200);
        assertThat(rankingExistente.getMejorRacha()).isEqualTo(5);
    }

    // ─── rankingGlobal() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("rankingGlobal() - retorna top 10 sin categoría")
    void rankingGlobal_retornaLista() {
        Ranking r = new Ranking();
        r.setIdUsuario(10L);
        r.setMejorPuntuacion(500);
        when(rankingRepo.findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc())
                .thenReturn(List.of(r));

        List<Ranking> resultado = rankingService.rankingGlobal();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMejorPuntuacion()).isEqualTo(500);
        verify(rankingRepo).findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc();
    }

    @Test
    @DisplayName("rankingGlobal() - sin registros → lista vacía")
    void rankingGlobal_listaVacia() {
        when(rankingRepo.findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc())
                .thenReturn(List.of());

        assertThat(rankingService.rankingGlobal()).isEmpty();
    }

    // ─── rankingPorCategoria() ────────────────────────────────────────────────

    @Test
    @DisplayName("rankingPorCategoria() - retorna top 10 de la categoría indicada")
    void rankingPorCategoria_retornaLista() {
        Ranking r = new Ranking();
        r.setCategoria(Categoria.ANIMALES);
        r.setMejorPuntuacion(300);
        when(rankingRepo.findTop10ByCategoriaOrderByMejorPuntuacionDesc(Categoria.ANIMALES))
                .thenReturn(List.of(r));

        List<Ranking> resultado = rankingService.rankingPorCategoria(Categoria.ANIMALES);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCategoria()).isEqualTo(Categoria.ANIMALES);
        verify(rankingRepo).findTop10ByCategoriaOrderByMejorPuntuacionDesc(Categoria.ANIMALES);
    }

    // ─── estadisticasUsuario() ────────────────────────────────────────────────

    @Test
    @DisplayName("estadisticasUsuario() - retorna todas las entradas del usuario")
    void estadisticasUsuario_retornaLista() {
        Ranking rBasico = new Ranking();
        rBasico.setIdUsuario(10L);
        rBasico.setCategoria(Categoria.BASICO);

        Ranking rGlobal = new Ranking();
        rGlobal.setIdUsuario(10L);

        when(rankingRepo.findByIdUsuario(10L)).thenReturn(List.of(rBasico, rGlobal));

        List<Ranking> resultado = rankingService.estadisticasUsuario(10L);

        assertThat(resultado).hasSize(2);
        verify(rankingRepo).findByIdUsuario(10L);
    }

    @Test
    @DisplayName("estadisticasUsuario() - usuario sin estadísticas → lista vacía")
    void estadisticasUsuario_sinEstadisticas_listaVacia() {
        when(rankingRepo.findByIdUsuario(99L)).thenReturn(List.of());

        assertThat(rankingService.estadisticasUsuario(99L)).isEmpty();
    }

    // ─── Ranking.getPrecision() ───────────────────────────────────────────────

    @Test
    @DisplayName("Ranking.getPrecision() - con palabras → calcula porcentaje correcto")
    void ranking_getPrecision_calculaCorrectamente() {
        Ranking r = new Ranking();
        r.setTotalPalabrasCorrectas(8);
        r.setTotalPalabras(10);

        assertThat(r.getPrecision()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Ranking.getPrecision() - sin palabras → retorna 0.0")
    void ranking_getPrecision_sinPalabras_retornaCero() {
        Ranking r = new Ranking();
        r.setTotalPalabras(0);

        assertThat(r.getPrecision()).isEqualTo(0.0);
    }
}

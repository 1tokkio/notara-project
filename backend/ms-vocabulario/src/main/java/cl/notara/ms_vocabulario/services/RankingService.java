package cl.notara.ms_vocabulario.services;

import cl.notara.ms_vocabulario.models.Partida;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.repositories.RankingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingService {

    private final RankingRepository rankingRepo;

    public RankingService(RankingRepository rankingRepo) {
        this.rankingRepo = rankingRepo;
    }

    @Transactional
    public void actualizarRanking(Partida partida) {
        actualizarEntrada(partida, partida.getCategoria());
        actualizarEntrada(partida, null); // global
    }

    private void actualizarEntrada(Partida partida, cl.notara.ms_vocabulario.models.Categoria categoria) {
        Ranking ranking = categoria == null
                ? rankingRepo.findByIdUsuarioAndCategoriaIsNull(partida.getIdUsuario())
                             .orElseGet(() -> crearNuevo(partida, null))
                : rankingRepo.findByIdUsuarioAndCategoria(partida.getIdUsuario(), categoria)
                             .orElseGet(() -> crearNuevo(partida, categoria));

        ranking.setNombreUsuario(partida.getNombreUsuario());
        ranking.setTotalPartidas(ranking.getTotalPartidas() + 1);
        ranking.setPuntuacionTotal(ranking.getPuntuacionTotal() + partida.getPuntuacion());
        ranking.setTotalPalabrasCorrectas(ranking.getTotalPalabrasCorrectas() + partida.getPalabrasCorrectas());
        ranking.setTotalPalabras(ranking.getTotalPalabras() + partida.getTotalPreguntas());

        if (partida.getPuntuacion() > ranking.getMejorPuntuacion()) {
            ranking.setMejorPuntuacion(partida.getPuntuacion());
        }
        if (partida.getMejorRacha() > ranking.getMejorRacha()) {
            ranking.setMejorRacha(partida.getMejorRacha());
        }

        rankingRepo.save(ranking);
    }

    public List<Ranking> rankingGlobal() {
        return rankingRepo.findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc();
    }

    public List<Ranking> rankingPorCategoria(cl.notara.ms_vocabulario.models.Categoria categoria) {
        return rankingRepo.findTop10ByCategoriaOrderByMejorPuntuacionDesc(categoria);
    }

    public List<Ranking> estadisticasUsuario(Long idUsuario) {
        return rankingRepo.findByIdUsuario(idUsuario);
    }

    private Ranking crearNuevo(Partida partida, cl.notara.ms_vocabulario.models.Categoria categoria) {
        Ranking r = new Ranking();
        r.setIdUsuario(partida.getIdUsuario());
        r.setNombreUsuario(partida.getNombreUsuario());
        r.setCategoria(categoria);
        return r;
    }
}

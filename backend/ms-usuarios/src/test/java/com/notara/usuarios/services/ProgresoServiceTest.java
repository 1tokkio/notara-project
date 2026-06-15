package com.notara.usuarios.services;

import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.repositories.ProgresoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ProgresoServiceTest {

    private ProgresoRepository progresoRepository;
    private ProgresoService    progresoService;

    @BeforeEach
    void setUp() {
        progresoRepository = mock(ProgresoRepository.class);
        progresoService    = new ProgresoService(progresoRepository);
    }

    // ──────────────────── getOrCreate ────────────────────

    @Test
    @DisplayName("getOrCreate - retorna progreso existente si ya existe")
    void getOrCreate_existente() {
        Progreso existing = new Progreso();
        existing.setUsuarioEmail("user@test.com");
        existing.setXp(100);

        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));

        Progreso result = progresoService.getOrCreate("user@test.com");

        assertEquals("user@test.com", result.getUsuarioEmail());
        assertEquals(100, result.getXp());
        verify(progresoRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreate - crea y guarda progreso nuevo si no existe")
    void getOrCreate_nuevo() {
        when(progresoRepository.findByUsuarioEmail("nuevo@test.com"))
                .thenReturn(Optional.empty());
        when(progresoRepository.save(any(Progreso.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Progreso result = progresoService.getOrCreate("nuevo@test.com");

        assertEquals("nuevo@test.com", result.getUsuarioEmail());
        verify(progresoRepository).save(any(Progreso.class));
    }

    // ──────────────────── sync ────────────────────

    @Test
    @DisplayName("sync - actualiza xp al valor más alto cuando dto > existente")
    void sync_xpSuperior() {
        Progreso existing = progresoConDefaults("user@test.com", 50, 3, 10, 2);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setXp(200);

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(200, result.getXp());
    }

    @Test
    @DisplayName("sync - NO baja xp si dto < existente (anti-regresión)")
    void sync_xpInferior_noRegresa() {
        Progreso existing = progresoConDefaults("user@test.com", 500, 5, 10, 2);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setXp(10); // menor que 500

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(500, result.getXp());
    }

    @Test
    @DisplayName("sync - streak actualiza al valor más alto")
    void sync_streakSuperior() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 3, 0, 0);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setStreak(10);

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(10, result.getStreak());
    }

    @Test
    @DisplayName("sync - wordsTotal actualiza al valor más alto")
    void sync_wordsTotalSuperior() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 0, 50, 0);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setWordsTotal(100);

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(100, result.getWordsTotal());
    }

    @Test
    @DisplayName("sync - songsCompleted actualiza al valor más alto")
    void sync_songsCompletedSuperior() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 0, 0, 5);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setSongsCompleted(20);

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(20, result.getSongsCompleted());
    }

    @Test
    @DisplayName("sync - exercisesToday siempre se sobreescribe")
    void sync_exercisesTodaySobreescribe() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 0, 0, 0);
        existing.setExercisesToday(10);
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setExercisesToday(3); // menor, pero se sobreescribe igual

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(3, result.getExercisesToday());
    }

    @Test
    @DisplayName("sync - lastStudyDate se sobreescribe siempre")
    void sync_lastStudyDateSobreescribe() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 0, 0, 0);
        existing.setLastStudyDate("2024-01-01");
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setLastStudyDate("2025-05-30");

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals("2025-05-30", result.getLastStudyDate());
    }

    @Test
    @DisplayName("sync - completedSongIds se sobreescribe siempre")
    void sync_completedSongIdsSobreescribe() {
        Progreso existing = progresoConDefaults("user@test.com", 0, 0, 0, 0);
        existing.setCompletedSongIds("1,2,3");
        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto();
        dto.setCompletedSongIds("1,2,3,4,5");

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals("1,2,3,4,5", result.getCompletedSongIds());
    }

    @Test
    @DisplayName("sync - dto con todos los campos null no modifica el progreso existente")
    void sync_dtoVacio_noModifica() {
        Progreso existing = progresoConDefaults("user@test.com", 100, 7, 50, 3);
        existing.setExercisesToday(5);
        existing.setLastStudyDate("2025-01-01");
        existing.setCompletedSongIds("1,2");

        when(progresoRepository.findByUsuarioEmail("user@test.com"))
                .thenReturn(Optional.of(existing));
        when(progresoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgresoDto dto = new ProgresoDto(); // todos null

        Progreso result = progresoService.sync("user@test.com", dto);

        assertEquals(100, result.getXp());
        assertEquals(7,   result.getStreak());
        assertEquals(50,  result.getWordsTotal());
        assertEquals(3,   result.getSongsCompleted());
        assertEquals(5,   result.getExercisesToday());
        assertEquals("2025-01-01", result.getLastStudyDate());
        assertEquals("1,2", result.getCompletedSongIds());
    }

    // Helper
    private Progreso progresoConDefaults(String email, int xp, int streak, int words, int songs) {
        Progreso p = new Progreso();
        p.setUsuarioEmail(email);
        p.setXp(xp);
        p.setStreak(streak);
        p.setWordsTotal(words);
        p.setSongsCompleted(songs);
        return p;
    }
}

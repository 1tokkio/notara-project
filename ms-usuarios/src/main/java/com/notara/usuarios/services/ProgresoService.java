package com.notara.usuarios.services;

import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.repositories.ProgresoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProgresoService {

    private final ProgresoRepository progresoRepository;

    public ProgresoService(ProgresoRepository progresoRepository) {
        this.progresoRepository = progresoRepository;
    }

    public Progreso getOrCreate(String email) {
        return progresoRepository.findByUsuarioEmail(email)
                .orElseGet(() -> {
                    Progreso p = new Progreso();
                    p.setUsuarioEmail(email);
                    return progresoRepository.save(p);
                });
    }

    public Progreso sync(String email, ProgresoDto dto) {
        Progreso p = getOrCreate(email);

        // Tomar siempre el valor más alto para evitar regresión de progreso
        if (dto.getXp() != null)
            p.setXp(Math.max(p.getXp(), dto.getXp()));
        if (dto.getStreak() != null)
            p.setStreak(Math.max(p.getStreak(), dto.getStreak()));
        if (dto.getWordsTotal() != null)
            p.setWordsTotal(Math.max(p.getWordsTotal(), dto.getWordsTotal()));
        if (dto.getSongsCompleted() != null)
            p.setSongsCompleted(Math.max(p.getSongsCompleted(), dto.getSongsCompleted()));

        // Ejercicios diarios y fecha se sobreescriben siempre
        if (dto.getExercisesToday() != null)
            p.setExercisesToday(dto.getExercisesToday());
        if (dto.getLastStudyDate() != null)
            p.setLastStudyDate(dto.getLastStudyDate());
        if (dto.getCompletedSongIds() != null)
            p.setCompletedSongIds(dto.getCompletedSongIds());

        return progresoRepository.save(p);
    }
}

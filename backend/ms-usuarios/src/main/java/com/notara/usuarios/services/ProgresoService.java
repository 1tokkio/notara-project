package com.notara.usuarios.services;

import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.repositories.ProgresoRepository;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada
 * con el progreso de los usuarios.
 *
 * <p>
 * Permite consultar, crear y sincronizar las estadísticas de avance
 * de un usuario dentro de la plataforma. Entre los datos gestionados
 * se encuentran la experiencia acumulada (XP), racha de estudio,
 * palabras aprendidas, canciones completadas y actividad diaria.
 * </p>
 *
 * <p>
 * Este servicio actúa como intermediario entre los controladores
 * y el repositorio de persistencia, garantizando la integridad
 * de los datos almacenados.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class ProgresoService {

    /**
     * Repositorio encargado de la persistencia de los datos de progreso.
     */
    private final ProgresoRepository progresoRepository;

    /**
     * Constructor que inyecta el repositorio de progreso.
     *
     * @param progresoRepository repositorio de progreso
     */
    public ProgresoService(ProgresoRepository progresoRepository) {
        this.progresoRepository = progresoRepository;
    }

    /**
     * Obtiene el progreso asociado a un usuario.
     *
     * <p>
     * Si no existe un registro para el correo electrónico indicado,
     * se crea automáticamente un nuevo registro con valores iniciales.
     * </p>
     *
     * @param email correo electrónico del usuario
     * @return progreso existente o recién creado
     */
    public Progreso getOrCreate(String email) {
        return progresoRepository.findByUsuarioEmail(email)
                .orElseGet(() -> {
                    Progreso p = new Progreso();
                    p.setUsuarioEmail(email);
                    return progresoRepository.save(p);
                });
    }

    /**
     * Sincroniza el progreso recibido desde el cliente con el
     * almacenado en la base de datos.
     *
     * <p>
     * Para evitar la pérdida de avance, los indicadores acumulativos
     * utilizan siempre el valor más alto entre el existente y el recibido.
     * </p>
     *
     * <ul>
     *     <li>XP (Experiencia)</li>
     *     <li>Racha de estudio (Streak)</li>
     *     <li>Total de palabras aprendidas</li>
     *     <li>Canciones completadas</li>
     * </ul>
     *
     * <p>
     * Los datos de actividad diaria y listas de progreso se actualizan
     * directamente con la información enviada por el cliente.
     * </p>
     *
     * @param email correo electrónico del usuario autenticado
     * @param dto datos de progreso enviados por el cliente
     * @return progreso actualizado y almacenado
     */
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

        // Datos que se actualizan directamente
        if (dto.getExercisesToday() != null)
            p.setExercisesToday(dto.getExercisesToday());

        if (dto.getLastStudyDate() != null)
            p.setLastStudyDate(dto.getLastStudyDate());

        if (dto.getCompletedSongIds() != null)
            p.setCompletedSongIds(dto.getCompletedSongIds());

        return progresoRepository.save(p);
    }
}

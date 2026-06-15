package com.notara.usuarios.models;

import jakarta.persistence.*;

/**
 * Entidad que representa el progreso académico y las estadísticas
 * de aprendizaje de un usuario dentro de la plataforma.
 *
 * <p>
 * Esta clase se encuentra asociada a la tabla {@code progreso} y almacena
 * información relacionada con el avance del usuario, incluyendo experiencia,
 * rachas de estudio, palabras aprendidas, canciones completadas y actividad diaria.
 * </p>
 *
 * <p>
 * Cada registro de progreso está asociado de forma única a un usuario
 * mediante su correo electrónico.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Entity
@Table(name = "progreso")
public class Progreso {

    /**
     * Identificador único del registro de progreso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Correo electrónico del usuario asociado al progreso.
     *
     * <p>
     * Este valor es único dentro de la tabla para garantizar
     * que cada usuario posea un único registro de progreso.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String usuarioEmail;

    /**
     * Puntos de experiencia acumulados por el usuario.
     */
    @Column(nullable = false)
    private Integer xp = 0;

    /**
     * Racha consecutiva de días de estudio.
     */
    @Column(nullable = false)
    private Integer streak = 0;

    /**
     * Cantidad total de palabras aprendidas.
     */
    @Column(nullable = false)
    private Integer wordsTotal = 0;

    /**
     * Número total de canciones completadas.
     */
    @Column(nullable = false)
    private Integer songsCompleted = 0;

    /**
     * Cantidad de ejercicios realizados durante el día actual.
     */
    @Column(nullable = false)
    private Integer exercisesToday = 0;

    /**
     * Fecha de la última sesión de estudio registrada.
     */
    private String lastStudyDate;

    /**
     * Identificadores de canciones completadas por el usuario.
     *
     * <p>
     * Se almacena como texto para permitir guardar múltiples identificadores
     * en una única columna.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String completedSongIds;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Progreso() {}

    /**
     * Obtiene el identificador del registro.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Obtiene el correo electrónico asociado al progreso.
     *
     * @return correo electrónico del usuario
     */
    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    /**
     * Asigna el correo electrónico del usuario.
     *
     * @param usuarioEmail correo electrónico a asociar
     */
    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    /**
     * Obtiene la experiencia acumulada.
     *
     * @return puntos de experiencia
     */
    public Integer getXp() {
        return xp;
    }

    /**
     * Asigna los puntos de experiencia.
     *
     * @param xp experiencia acumulada
     */
    public void setXp(Integer xp) {
        this.xp = xp;
    }

    /**
     * Obtiene la racha de estudio actual.
     *
     * @return racha de estudio
     */
    public Integer getStreak() {
        return streak;
    }

    /**
     * Asigna la racha de estudio.
     *
     * @param streak nueva racha
     */
    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    /**
     * Obtiene el total de palabras aprendidas.
     *
     * @return cantidad de palabras aprendidas
     */
    public Integer getWordsTotal() {
        return wordsTotal;
    }

    /**
     * Asigna el total de palabras aprendidas.
     *
     * @param wordsTotal cantidad de palabras aprendidas
     */
    public void setWordsTotal(Integer wordsTotal) {
        this.wordsTotal = wordsTotal;
    }

    /**
     * Obtiene la cantidad de canciones completadas.
     *
     * @return canciones completadas
     */
    public Integer getSongsCompleted() {
        return songsCompleted;
    }

    /**
     * Asigna la cantidad de canciones completadas.
     *
     * @param songsCompleted número de canciones completadas
     */
    public void setSongsCompleted(Integer songsCompleted) {
        this.songsCompleted = songsCompleted;
    }

    /**
     * Obtiene la cantidad de ejercicios realizados hoy.
     *
     * @return ejercicios realizados
     */
    public Integer getExercisesToday() {
        return exercisesToday;
    }

    /**
     * Asigna la cantidad de ejercicios realizados hoy.
     *
     * @param exercisesToday ejercicios realizados
     */
    public void setExercisesToday(Integer exercisesToday) {
        this.exercisesToday = exercisesToday;
    }

    /**
     * Obtiene la fecha de la última sesión de estudio.
     *
     * @return fecha de estudio
     */
    public String getLastStudyDate() {
        return lastStudyDate;
    }

    /**
     * Asigna la fecha de la última sesión de estudio.
     *
     * @param lastStudyDate fecha a registrar
     */
    public void setLastStudyDate(String lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    /**
     * Obtiene los identificadores de canciones completadas.
     *
     * @return lista de identificadores almacenada como texto
     */
    public String getCompletedSongIds() {
        return completedSongIds;
    }

    /**
     * Asigna los identificadores de canciones completadas.
     *
     * @param completedSongIds identificadores a almacenar
     */
    public void setCompletedSongIds(String completedSongIds) {
        this.completedSongIds = completedSongIds;
    }
}

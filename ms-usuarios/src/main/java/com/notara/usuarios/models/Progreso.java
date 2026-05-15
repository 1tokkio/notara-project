package com.notara.usuarios.models;

import jakarta.persistence.*;

@Entity
@Table(name = "progreso")
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuarioEmail;

    @Column(nullable = false)
    private Integer xp = 0;

    @Column(nullable = false)
    private Integer streak = 0;

    @Column(nullable = false)
    private Integer wordsTotal = 0;

    @Column(nullable = false)
    private Integer songsCompleted = 0;

    @Column(nullable = false)
    private Integer exercisesToday = 0;

    private String lastStudyDate;

    @Column(columnDefinition = "TEXT")
    private String completedSongIds;

    public Progreso() {}

    public Long getId() { return id; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public Integer getXp() { return xp; }
    public void setXp(Integer xp) { this.xp = xp; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public Integer getWordsTotal() { return wordsTotal; }
    public void setWordsTotal(Integer wordsTotal) { this.wordsTotal = wordsTotal; }

    public Integer getSongsCompleted() { return songsCompleted; }
    public void setSongsCompleted(Integer songsCompleted) { this.songsCompleted = songsCompleted; }

    public Integer getExercisesToday() { return exercisesToday; }
    public void setExercisesToday(Integer exercisesToday) { this.exercisesToday = exercisesToday; }

    public String getLastStudyDate() { return lastStudyDate; }
    public void setLastStudyDate(String lastStudyDate) { this.lastStudyDate = lastStudyDate; }

    public String getCompletedSongIds() { return completedSongIds; }
    public void setCompletedSongIds(String completedSongIds) { this.completedSongIds = completedSongIds; }
}

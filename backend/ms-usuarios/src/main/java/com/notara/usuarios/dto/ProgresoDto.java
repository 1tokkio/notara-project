package com.notara.usuarios.dto;

public class ProgresoDto {

    private Integer xp;
    private Integer streak;
    private Integer wordsTotal;
    private Integer songsCompleted;
    private Integer exercisesToday;
    private String lastStudyDate;
    private String completedSongIds;

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

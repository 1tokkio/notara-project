package cl.notara.ms_vocabulario.dto;

public class RespuestaDTO {

    private boolean esCorrecta;
    private boolean tiempoAgotado;
    private String respuestaCorrecta;
    private String respuestaUsuario;
    private int puntosObtenidos;
    private int puntuacionActual;
    private int rachaActual;
    private int palabrasCorrectas;
    private int numeroPregunta;
    private int totalPreguntas;
    private boolean gameOver;
    private PreguntaDTO siguientePregunta;
    private PartidaResumenDTO resumen;

    public boolean isEsCorrecta() { return esCorrecta; }
    public void setEsCorrecta(boolean esCorrecta) { this.esCorrecta = esCorrecta; }

    public boolean isTiempoAgotado() { return tiempoAgotado; }
    public void setTiempoAgotado(boolean tiempoAgotado) { this.tiempoAgotado = tiempoAgotado; }

    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public void setRespuestaCorrecta(String respuestaCorrecta) { this.respuestaCorrecta = respuestaCorrecta; }

    public String getRespuestaUsuario() { return respuestaUsuario; }
    public void setRespuestaUsuario(String respuestaUsuario) { this.respuestaUsuario = respuestaUsuario; }

    public int getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }

    public int getPuntuacionActual() { return puntuacionActual; }
    public void setPuntuacionActual(int puntuacionActual) { this.puntuacionActual = puntuacionActual; }

    public int getRachaActual() { return rachaActual; }
    public void setRachaActual(int rachaActual) { this.rachaActual = rachaActual; }

    public int getPalabrasCorrectas() { return palabrasCorrectas; }
    public void setPalabrasCorrectas(int palabrasCorrectas) { this.palabrasCorrectas = palabrasCorrectas; }

    public int getNumeroPregunta() { return numeroPregunta; }
    public void setNumeroPregunta(int numeroPregunta) { this.numeroPregunta = numeroPregunta; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public PreguntaDTO getSiguientePregunta() { return siguientePregunta; }
    public void setSiguientePregunta(PreguntaDTO siguientePregunta) { this.siguientePregunta = siguientePregunta; }

    public PartidaResumenDTO getResumen() { return resumen; }
    public void setResumen(PartidaResumenDTO resumen) { this.resumen = resumen; }
}

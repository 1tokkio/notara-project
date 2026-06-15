package cl.notara.ms_vocabulario.dto;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Dificultad;

import java.time.LocalDateTime;

public class PreguntaDTO {

    private Long idPregunta;
    private Long idPartida;
    private String definicion;
    private String pista;
    private Categoria categoria;
    private Dificultad dificultad;
    private int numeroPregunta;
    private int totalPreguntas;
    private int puntuacionActual;
    private int rachaActual;
    private int tiempoMaximoSegundos;
    private LocalDateTime fechaEntregada;

    public Long getIdPregunta() { return idPregunta; }
    public void setIdPregunta(Long idPregunta) { this.idPregunta = idPregunta; }

    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public String getDefinicion() { return definicion; }
    public void setDefinicion(String definicion) { this.definicion = definicion; }

    public String getPista() { return pista; }
    public void setPista(String pista) { this.pista = pista; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Dificultad getDificultad() { return dificultad; }
    public void setDificultad(Dificultad dificultad) { this.dificultad = dificultad; }

    public int getNumeroPregunta() { return numeroPregunta; }
    public void setNumeroPregunta(int numeroPregunta) { this.numeroPregunta = numeroPregunta; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    public int getPuntuacionActual() { return puntuacionActual; }
    public void setPuntuacionActual(int puntuacionActual) { this.puntuacionActual = puntuacionActual; }

    public int getRachaActual() { return rachaActual; }
    public void setRachaActual(int rachaActual) { this.rachaActual = rachaActual; }

    public int getTiempoMaximoSegundos() { return tiempoMaximoSegundos; }
    public void setTiempoMaximoSegundos(int tiempoMaximoSegundos) { this.tiempoMaximoSegundos = tiempoMaximoSegundos; }

    public LocalDateTime getFechaEntregada() { return fechaEntregada; }
    public void setFechaEntregada(LocalDateTime fechaEntregada) { this.fechaEntregada = fechaEntregada; }
}

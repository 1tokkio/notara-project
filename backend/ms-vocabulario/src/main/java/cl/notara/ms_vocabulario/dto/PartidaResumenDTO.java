package cl.notara.ms_vocabulario.dto;

import cl.notara.ms_vocabulario.models.Categoria;

public class PartidaResumenDTO {

    private Long idPartida;
    private Long idUsuario;
    private String nombreUsuario;
    private Categoria categoria;
    private int puntuacionFinal;
    private int palabrasCorrectas;
    private int totalPreguntas;
    private double precision;
    private int mejorRacha;
    private String calificacion;

    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getPuntuacionFinal() { return puntuacionFinal; }
    public void setPuntuacionFinal(int puntuacionFinal) { this.puntuacionFinal = puntuacionFinal; }

    public int getPalabrasCorrectas() { return palabrasCorrectas; }
    public void setPalabrasCorrectas(int palabrasCorrectas) { this.palabrasCorrectas = palabrasCorrectas; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    public double getPrecision() { return precision; }
    public void setPrecision(double precision) { this.precision = precision; }

    public int getMejorRacha() { return mejorRacha; }
    public void setMejorRacha(int mejorRacha) { this.mejorRacha = mejorRacha; }

    public String getCalificacion() { return calificacion; }
    public void setCalificacion(String calificacion) { this.calificacion = calificacion; }
}

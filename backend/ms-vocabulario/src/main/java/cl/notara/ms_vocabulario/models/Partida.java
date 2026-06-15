package cl.notara.ms_vocabulario.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idUsuario;

    @Column(nullable = false)
    private String nombreUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartida estado = EstadoPartida.EN_CURSO;

    @Column(nullable = false)
    private int puntuacion = 0;

    @Column(nullable = false)
    private int preguntaActualIndex = 0;

    @Column(nullable = false)
    private int totalPreguntas = 10;

    @Column(nullable = false)
    private int rachaActual = 0;

    @Column(nullable = false)
    private int mejorRacha = 0;

    @Column(nullable = false)
    private int palabrasCorrectas = 0;

    @Column(nullable = false)
    private int tiempoMaximoSegundos = 30;

    @Column(nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    private LocalDateTime fechaFin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public EstadoPartida getEstado() { return estado; }
    public void setEstado(EstadoPartida estado) { this.estado = estado; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    public int getPreguntaActualIndex() { return preguntaActualIndex; }
    public void setPreguntaActualIndex(int preguntaActualIndex) { this.preguntaActualIndex = preguntaActualIndex; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    public int getRachaActual() { return rachaActual; }
    public void setRachaActual(int rachaActual) { this.rachaActual = rachaActual; }

    public int getMejorRacha() { return mejorRacha; }
    public void setMejorRacha(int mejorRacha) { this.mejorRacha = mejorRacha; }

    public int getPalabrasCorrectas() { return palabrasCorrectas; }
    public void setPalabrasCorrectas(int palabrasCorrectas) { this.palabrasCorrectas = palabrasCorrectas; }

    public int getTiempoMaximoSegundos() { return tiempoMaximoSegundos; }
    public void setTiempoMaximoSegundos(int tiempoMaximoSegundos) { this.tiempoMaximoSegundos = tiempoMaximoSegundos; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
}

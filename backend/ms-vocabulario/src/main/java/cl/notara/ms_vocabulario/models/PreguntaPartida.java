package cl.notara.ms_vocabulario.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "preguntas_partida")
public class PreguntaPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "palabra_id", nullable = false)
    private Palabra palabra;

    @Column(nullable = false)
    private int orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPregunta estado = EstadoPregunta.PENDIENTE;

    private String respuestaUsuario;

    private Boolean esCorrecta;

    private Long tiempoRespuestaMs;

    @Column(nullable = false)
    private int puntosObtenidos = 0;

    private LocalDateTime fechaEntregada;

    private LocalDateTime fechaRespondida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public Palabra getPalabra() { return palabra; }
    public void setPalabra(Palabra palabra) { this.palabra = palabra; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public EstadoPregunta getEstado() { return estado; }
    public void setEstado(EstadoPregunta estado) { this.estado = estado; }

    public String getRespuestaUsuario() { return respuestaUsuario; }
    public void setRespuestaUsuario(String respuestaUsuario) { this.respuestaUsuario = respuestaUsuario; }

    public Boolean getEsCorrecta() { return esCorrecta; }
    public void setEsCorrecta(Boolean esCorrecta) { this.esCorrecta = esCorrecta; }

    public Long getTiempoRespuestaMs() { return tiempoRespuestaMs; }
    public void setTiempoRespuestaMs(Long tiempoRespuestaMs) { this.tiempoRespuestaMs = tiempoRespuestaMs; }

    public int getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }

    public LocalDateTime getFechaEntregada() { return fechaEntregada; }
    public void setFechaEntregada(LocalDateTime fechaEntregada) { this.fechaEntregada = fechaEntregada; }

    public LocalDateTime getFechaRespondida() { return fechaRespondida; }
    public void setFechaRespondida(LocalDateTime fechaRespondida) { this.fechaRespondida = fechaRespondida; }
}

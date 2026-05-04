package cl.notara.ms_notas_metas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Column(length = 500)
    private String contenido;

    @NotNull(message = "El id del Usuario es obligatorio")
    @Column(nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    private EstadoNota estado;

    public Nota() {}

    public Nota(Long id, String titulo, String contenido, Long idUsuario,EstadoNota estado) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public EstadoNota getEstado() {
        return estado;
    }

    public void setEstado(EstadoNota estado) {
        this.estado = estado;
    }
}


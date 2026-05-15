package cl.notara.ms_notas_metas.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la meta es obligatorio")
    private String nombre;

    private String descripcion;

    private LocalDate fechaLimite;

    private boolean completada = false;

    @NotNull(message = "El idUsuario es obligatorio")
    @Column(nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    private EstadoMeta estado;

    public Meta() {}

    public Meta(Long id, String nombre, String descripcion, LocalDate fechaLimite, Long idUsuario, EstadoMeta estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public EstadoMeta getEstado() {
        return estado;
    }
    public void setEstado(EstadoMeta estado) {
        this.estado = estado;
    }
}

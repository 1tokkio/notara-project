package cl.notara.ms_vocabulario.dto;

import cl.notara.ms_vocabulario.models.Categoria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IniciarPartidaRequest {

    @NotNull(message = "El idUsuario es obligatorio")
    private Long idUsuario;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    private String nombreUsuario;

    @NotNull(message = "La categoría es obligatoria")
    private Categoria categoria;

    @Min(value = 5, message = "Mínimo 5 preguntas")
    @Max(value = 20, message = "Máximo 20 preguntas")
    private int totalPreguntas = 10;

    @Min(value = 10, message = "Mínimo 10 segundos")
    @Max(value = 120, message = "Máximo 120 segundos")
    private int tiempoMaximoSegundos = 30;

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(int totalPreguntas) { this.totalPreguntas = totalPreguntas; }

    public int getTiempoMaximoSegundos() { return tiempoMaximoSegundos; }
    public void setTiempoMaximoSegundos(int tiempoMaximoSegundos) { this.tiempoMaximoSegundos = tiempoMaximoSegundos; }
}

package cl.notara.ms_vocabulario.dto;

import jakarta.validation.constraints.NotBlank;

public class ResponderRequest {

    @NotBlank(message = "La respuesta no puede estar vacía")
    private String respuesta;

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
}

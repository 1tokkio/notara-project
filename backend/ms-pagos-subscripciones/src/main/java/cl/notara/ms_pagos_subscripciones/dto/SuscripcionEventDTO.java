package cl.notara.ms_pagos_subscripciones.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SuscripcionEventDTO {

    private Long idSuscripcion;
    private Long idUsuario;
    private String emailDestinatario;
    private String nombreUsuario;
    private String plan;
    private String estado;
    private String tipoEvento;
    private Double monto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaEvento;

    public SuscripcionEventDTO() {}

    public SuscripcionEventDTO(Long idSuscripcion, Long idUsuario, String emailDestinatario,
                                String nombreUsuario, String plan, String estado,
                                String tipoEvento, Double monto,
                                LocalDate fechaInicio, LocalDate fechaFin) {
        this.idSuscripcion    = idSuscripcion;
        this.idUsuario        = idUsuario;
        this.emailDestinatario = emailDestinatario;
        this.nombreUsuario    = nombreUsuario;
        this.plan             = plan;
        this.estado           = estado;
        this.tipoEvento       = tipoEvento;
        this.monto            = monto;
        this.fechaInicio      = fechaInicio;
        this.fechaFin         = fechaFin;
        this.fechaEvento      = LocalDateTime.now();
    }

    public Long getIdSuscripcion() { return idSuscripcion; }
    public void setIdSuscripcion(Long idSuscripcion) { this.idSuscripcion = idSuscripcion; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getEmailDestinatario() { return emailDestinatario; }
    public void setEmailDestinatario(String emailDestinatario) { this.emailDestinatario = emailDestinatario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
}

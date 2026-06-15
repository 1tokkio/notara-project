package cl.notara.ms_vocabulario.models;

import jakarta.persistence.*;

@Entity
@Table(name = "rankings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "categoria"}))
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(nullable = false)
    private String nombreUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Categoria categoria;

    @Column(nullable = false)
    private int mejorPuntuacion = 0;

    @Column(nullable = false)
    private long puntuacionTotal = 0;

    @Column(nullable = false)
    private int totalPartidas = 0;

    @Column(nullable = false)
    private int totalPalabrasCorrectas = 0;

    @Column(nullable = false)
    private int totalPalabras = 0;

    @Column(nullable = false)
    private int mejorRacha = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getMejorPuntuacion() { return mejorPuntuacion; }
    public void setMejorPuntuacion(int mejorPuntuacion) { this.mejorPuntuacion = mejorPuntuacion; }

    public long getPuntuacionTotal() { return puntuacionTotal; }
    public void setPuntuacionTotal(long puntuacionTotal) { this.puntuacionTotal = puntuacionTotal; }

    public int getTotalPartidas() { return totalPartidas; }
    public void setTotalPartidas(int totalPartidas) { this.totalPartidas = totalPartidas; }

    public int getTotalPalabrasCorrectas() { return totalPalabrasCorrectas; }
    public void setTotalPalabrasCorrectas(int totalPalabrasCorrectas) { this.totalPalabrasCorrectas = totalPalabrasCorrectas; }

    public int getTotalPalabras() { return totalPalabras; }
    public void setTotalPalabras(int totalPalabras) { this.totalPalabras = totalPalabras; }

    public int getMejorRacha() { return mejorRacha; }
    public void setMejorRacha(int mejorRacha) { this.mejorRacha = mejorRacha; }

    public double getPrecision() {
        return totalPalabras == 0 ? 0.0 : (double) totalPalabrasCorrectas / totalPalabras * 100;
    }
}

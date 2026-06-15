package cl.notara.ms_notas_metas.dto;

public class UsuarioDTO {

    private long id;
    private String nombre;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String nombre, long id) {
        this.nombre = nombre;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

package cl.notara.bff.dto;

import cl.notara.bff.model.Meta;
import cl.notara.bff.model.Nota;
import cl.notara.bff.model.Usuario;

import java.util.List;

public class DashboardDTO {

    private Usuario usuario;
    private List<Nota> notas;
    private List<Meta> metas;

    public DashboardDTO() {
    }

    public DashboardDTO(
            Usuario usuario,
            List<Nota> notas,
            List<Meta> metas) {

        this.usuario = usuario;
        this.notas = notas;
        this.metas = metas;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Nota> getNotas() {
        return notas;
    }

    public void setNotas(List<Nota> notas) {
        this.notas = notas;
    }

    public List<Meta> getMetas() {
        return metas;
    }

    public void setMetas(List<Meta> metas) {
        this.metas = metas;
    }
}

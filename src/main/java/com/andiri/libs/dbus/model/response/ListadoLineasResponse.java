package response;

import Avisos;
import Linea;

import java.util.List;

public class ListadoLineasResponse { // New POJO for listadoLineas
    private String estado;
    private Avisos avisos;
    private List<Linea> lista;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public List<Linea> getLista() { return lista; }
    public void setLista(List<Linea> lista) { this.lista = lista; }
}
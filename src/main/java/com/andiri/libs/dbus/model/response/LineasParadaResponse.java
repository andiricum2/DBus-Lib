package response;

import Avisos;
import Linea; // Import the Linea model
import java.util.List; // Import List

public class LineasParadaResponse {
    private String estado;
    private Avisos avisos;
    private List<Linea> lista; // Field to hold the list of Linea objects

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public List<Linea> getLista() { // Getter for the lista field
        return lista;
    }

    public void setLista(List<Linea> lista) { // Setter for the lista field
        this.lista = lista;
    }
}
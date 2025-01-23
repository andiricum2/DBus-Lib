package response;

import Avisos;
import Itinerario;

import java.util.List;

public class RecorridoLineaResponse {
    private String estado;
    private Avisos avisos;
    private List<Itinerario> itinerarios;

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public List<Itinerario> getItinerarios() { return itinerarios; }
    public void setItinerarios(List<Itinerario> itinerarios) { this.itinerarios = itinerarios; }
}
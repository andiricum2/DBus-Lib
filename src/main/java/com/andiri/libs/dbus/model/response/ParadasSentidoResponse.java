package response;

import Avisos;
import Parada;
import Itinerario; // Import Itinerario Model

import java.util.List;

public class ParadasSentidoResponse {
    private String estado;
    private Avisos avisos;
    private Itinerario itinerario; // Add field for the "itinerario" object
    private List<Parada> lista; // Rename 'paradas' to 'lista' to match JSON

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public Itinerario getItinerario() { // Getter for the itinerario object
        return itinerario;
    }

    public void setItinerario(Itinerario itinerario) { // Setter for the itinerario object
        this.itinerario = itinerario;
    }

    public List<Parada> getLista() { // Renamed getter to getLista
        return lista;
    }

    public void setLista(List<Parada> lista) { // Renamed setter to setLista
        this.lista = lista;
    }
}
package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Itinerario;
import com.andiri.libs.dbus.model.Parada;

import java.util.List;

public class ParadasItinerarioResponse {
    private String estado;
    private Avisos avisos;
    private Itinerario itinerario; // Add field for the root "itinerario" object
    private List<Parada> lista; // Changed from 'paradas' to 'lista' to match JSON array name

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public Itinerario getItinerario() { // Getter for the root itinerario object
        return itinerario;
    }

    public void setItinerario(Itinerario itinerario) { // Setter for the root itinerario object
        this.itinerario = itinerario;
    }

    public List<Parada> getLista() { // Changed getter to getLista to match field name 'lista'
        return lista;
    }

    public void setLista(List<Parada> lista) { // Changed setter to setLista to match field name 'lista'
        this.lista = lista;
    }
}
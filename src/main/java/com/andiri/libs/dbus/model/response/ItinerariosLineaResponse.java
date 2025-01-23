package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Itinerario;
import com.andiri.libs.dbus.model.Linea;

import java.util.List;

public class ItinerariosLineaResponse {
    private String estado; // Matches "estado" in JSON
    private Avisos avisos; // Matches "avisos" in JSON
    private Linea linea; // Matches "linea" in JSON (root linea object)
    private List<Itinerario> lista; // Matches "lista" in JSON

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public Linea getLinea() { return linea; }
    public void setLinea(Linea linea) { this.linea = linea; }
    public List<Itinerario> getLista() { return lista; }
    public void setLista(List<Itinerario> lista) { this.lista = lista; }
}
package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Itinerario;
import com.andiri.libs.dbus.model.Parada;
import com.andiri.libs.dbus.model.Punto;

import java.util.List;

public class PuntosParadaResponse {
    private String estado;
    private Avisos avisos;
    private List<Punto> puntos;

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public List<Punto> getPuntos() { // Renamed getter to getLista
        return puntos;
    }

    public void setPuntos(List<Punto> puntos) { // Renamed setter to setLista
        this.puntos = puntos;
    }
}
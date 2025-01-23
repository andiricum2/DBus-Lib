package com.andiri.libs.dbus.model.response;


import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Itinerario;

import java.util.List; // Importa la clase List

public class ExpedicionesParadaSentidoResponse {
    private String estado;
    private Avisos avisos;
    private Itinerario itinerario; // Usa la clase Itinerario que definimos previamente
    private List<String> horas; // Lista de Strings para las horas

    // Getters y Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public Itinerario getItinerario() { return itinerario; }
    public void setItinerario(Itinerario itinerario) { this.itinerario = itinerario; }

    public List<String> getHoras() { return horas; }
    public void setHoras(List<String> horas) { this.horas = horas; }
}
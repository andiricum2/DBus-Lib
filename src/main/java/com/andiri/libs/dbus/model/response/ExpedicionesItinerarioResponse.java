package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;

import java.util.List;

public class ExpedicionesItinerarioResponse {
    private String estado;
    private Avisos avisos;
    private Object itinerario; // Tipo Object por ahora, puedes refinarlo si conoces la estructura de "itinerario"
    private List<String> horas; // Lista de Strings para las horas, asumiendo que son horas en formato String

    // Getters y Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }

    public Object getItinerario() { return itinerario; }
    public void setItinerario(Object itinerario) { this.itinerario = itinerario; }

    public List<String> getHoras() { return horas; }
    public void setHoras(List<String> horas) { this.horas = horas; }
}
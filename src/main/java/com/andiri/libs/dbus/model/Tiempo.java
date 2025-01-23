package com.andiri.libs.dbus.model;

public class Tiempo {
    private Itinerario itinerario;
    private String creacion;
    private int minutos;
    private boolean cabecera;
    private int tipo;
    private String hora;

    // Getters and Setters...
    public Itinerario getItinerario() { return itinerario; }
    public void setItinerario(Itinerario itinerario) { this.itinerario = itinerario; }
    public String getCreacion() { return creacion; }
    public void setCreacion(String creacion) { this.creacion = creacion; }
    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }
    public boolean isCabecera() { return cabecera; }
    public void setCabecera(boolean cabecera) { this.cabecera = cabecera; }
    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
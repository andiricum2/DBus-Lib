package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Linea;

import java.util.List;

public class ListadoLineasResponse { // New POJO for listadoLineas
    private String estado;
    private Avisos avisos;
    private List<Linea> lineas;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public List<Linea> getLineas() { return lineas; }
    public void setLineas(List<Linea> lineas) { this.lineas = lineas; }
}
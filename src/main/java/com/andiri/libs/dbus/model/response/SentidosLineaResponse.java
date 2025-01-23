package com.andiri.libs.dbus.model.response;


import com.andiri.libs.dbus.model.Avisos;
import com.andiri.libs.dbus.model.Linea;
import com.andiri.libs.dbus.model.Sentido;

import java.util.List;

public class SentidosLineaResponse {
    private String estado;
    private Avisos avisos;
    private Linea linea;
    private List<Sentido> lista;

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public Linea getLinea() { return linea; }
    public void setLinea(Linea linea) { this.linea = linea; }
    public List<Sentido> getLista() { return lista; }
    public void setLista(List<Sentido> lista) { this.lista = lista; }
}
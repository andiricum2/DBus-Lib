package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;

public class AvisosResponse {
    private String estado;
    private Avisos avisos;

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
}
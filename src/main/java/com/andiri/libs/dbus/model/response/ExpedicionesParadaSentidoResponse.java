package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;

public class ExpedicionesParadaSentidoResponse {
    private String estado;
    private Avisos avisos;
    // Define fields specific to expedicionesParadaSentido response
    // ...

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
}
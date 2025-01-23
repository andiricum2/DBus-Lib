package com.andiri.libs.dbus.model.response;

import com.andiri.libs.dbus.model.Avisos;

public class DatosVehiculoResponse {
    private String estado;
    private Avisos avisos;
    // Define fields specific to datosVehiculo response if needed
    // Based on the name, it might contain Vehiculo data?

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
}
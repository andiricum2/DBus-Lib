package com.andiri.libs.dbus.model;

public class Punto {
    private Posicion pos;
    private int tipo;
    private Parada parada;
    private int ordenPunto;

    // Getters and Setters...
    public Posicion getPos() { return pos; }
    public void setPos(Posicion pos) { this.pos = pos; }
    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }
    public Parada getParada() { return parada; }
    public void setParada(Parada parada) { this.parada = parada; }
    public int getOrdenPunto() { return ordenPunto; }
    public void setOrdenPunto(int ordenPunto) { this.ordenPunto = ordenPunto; }
}
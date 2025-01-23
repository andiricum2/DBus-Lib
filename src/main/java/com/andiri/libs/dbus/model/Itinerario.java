package com.andiri.libs.dbus.model;

import java.util.List;

public class Itinerario {
    private String id;
    private String cod;
    private String destino;
    private String nombre;
    private Linea linea;
    private boolean actual;
    private List<Punto> puntos; // Only for recorridoLinea
    private int tipoRecorrido;
    private int colorRGB;
    private int habitual;

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Linea getLinea() { return linea; }
    public void setLinea(Linea linea) { this.linea = linea; }
    public boolean isActual() { return actual; }
    public void setActual(boolean actual) { this.actual = actual; }
    public List<Punto> getPuntos() { return puntos; }
    public void setPuntos(List<Punto> puntos) { this.puntos = puntos; }
    public int getTipoRecorrido() { return tipoRecorrido; }
    public void setTipoRecorrido(int tipoRecorrido) { this.tipoRecorrido = tipoRecorrido; }
    public int getColorRGB() { return colorRGB; }
    public void setColorRGB(int colorRGB) { this.colorRGB = colorRGB; }
    public int getHabitual() { return habitual; }
    public void setHabitual(int habitual) { this.habitual = habitual; }
}
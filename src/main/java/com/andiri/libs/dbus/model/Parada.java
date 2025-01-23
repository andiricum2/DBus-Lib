package com.andiri.libs.dbus.model;

public class Parada {
    private String id;
    private String cod;
    private String desc;
    private int ordenParada;
    private Boolean habitual;

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public int getOrdenParada() { return ordenParada; }
    public void setOrdenParada(int ordenParada) { this.ordenParada = ordenParada; }
    public Boolean getHabitual() { return habitual; }
    public void setHabitual(Boolean habitual) { this.habitual = habitual; }
}
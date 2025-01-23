package response;

import Avisos;
import Parada;
import Tiempo;

import java.util.List;

public class TiemposParadaResponse {
    private String estado;
    private Avisos avisos;
    private Parada parada;
    private List<Tiempo> tiempos;

    // Getters and Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Avisos getAvisos() { return avisos; }
    public void setAvisos(Avisos avisos) { this.avisos = avisos; }
    public Parada getParada() { return parada; }
    public void setParada(Parada parada) { this.parada = parada; }
    public List<Tiempo> getTiempos() { return tiempos; }
    public void setTiempos(List<Tiempo> tiempos) { this.tiempos = tiempos; }
}
package response;

import Avisos; // Asegúrate de que esta clase Avisos existe y representa un elemento de la lista de avisos
import java.util.List; // Importa la clase List

public class DatosVehiculoResponse {
    private String estado;
    private List<Avisos> avisos; // Cambiado a List<Avisos> para que coincida con el array del JSON
    private Object vehiculo;      // Tipo Object por ahora, puedes refinarlo si conoces la estructura de "vehiculo"
    private Object itinerarios;   // Tipo Object por ahora, puedes refinarlo si conoces la estructura de "itinerarios"
    private String idParadaActual;
    private String idItinerarioActual;

    // Getters y Setters...
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Avisos> getAvisos() { return avisos; } // Getter y Setter para List<Avisos>
    public void setAvisos(List<Avisos> avisos) { this.avisos = avisos; }

    public Object getVehiculo() { return vehiculo; }
    public void setVehiculo(Object vehiculo) { this.vehiculo = vehiculo; }

    public Object getItinerarios() { return itinerarios; }
    public void setItinerarios(Object itinerarios) { this.itinerarios = itinerarios; }

    public String getIdParadaActual() { return idParadaActual; }
    public void setIdParadaActual(String idParadaActual) { this.idParadaActual = idParadaActual; }

    public String getIdItinerarioActual() { return idItinerarioActual; }
    public void setIdItinerarioActual(String idItinerarioActual) { this.idItinerarioActual = idItinerarioActual; }
}
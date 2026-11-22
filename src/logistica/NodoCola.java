package logistica;

/** Enlace de un camión con el siguiente turno. */
public class NodoCola {
    final Camion camion;
    NodoCola siguiente;
    public NodoCola(Camion camion) { this.camion = camion; }
}

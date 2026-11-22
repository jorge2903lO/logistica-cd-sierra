package logistica;

/** Cola FIFO manual. Los dos extremos permiten altas y bajas en O(1). */
public class ColaCamiones {
    private NodoCola frente, fin;

    public void encolar(Camion camion) {
        NodoCola nuevo = new NodoCola(camion);
        if (fin == null) frente = nuevo;
        else fin.siguiente = nuevo;
        fin = nuevo;
    }

    public Camion desencolar() {
        if (estaVacia()) throw new IllegalStateException("No hay camiones disponibles en la cola.");
        Camion primero = frente.camion;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        return primero;
    }

    public boolean estaVacia() { return frente == null; }

    public boolean contienePlaca(String placa) {
        for (NodoCola n = frente; n != null; n = n.siguiente)
            if (n.camion.getPlaca().equalsIgnoreCase(placa)) return true;
        return false;
    }

    public String mostrar() {
        if (estaVacia()) return "No hay vehículos en espera.";
        StringBuilder filas = new StringBuilder();
        int puesto = 1;
        for (NodoCola n = frente; n != null; n = n.siguiente) {
            filas.append(puesto++).append(".  ").append(n.camion).append("\n");
        }
        return filas.toString().trim();
    }
}

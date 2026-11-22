package logistica;

/** Instantánea inmutable de una salida de almacén. */
public class Entrega {
    private final int id;
    private final String producto;
    private final String camion;
    private final String destino;
    private final int cantidad;

    public Entrega(int id, String producto, String camion, String destino, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.camion = camion;
        this.destino = destino;
        this.cantidad = cantidad;
    }

    public int getId() { return id; }

    public String getProducto() { return producto; }

    public String getCamion() { return camion; }

    public String getDestino() { return destino; }

    public int getCantidad() { return cantidad; }

    @Override public String toString() {
        return "#" + id + "  |  " + producto + "  |  " + cantidad + " u.  |  " + camion + "  |  " + destino;
    }
}

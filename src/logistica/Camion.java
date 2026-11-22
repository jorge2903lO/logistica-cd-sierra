package logistica;

/** Vehículo disponible para un turno de despacho. */
public class Camion {
    private final String placa;
    private final String conductor;
    private final double capacidadTon;

    public Camion(String placa, String conductor, double capacidadTon) {
        this.placa = placa;
        this.conductor = conductor;
        this.capacidadTon = capacidadTon;
    }

    public String getPlaca() { return placa; }

    public String getConductor() { return conductor; }

    public double getCapacidadTon() { return capacidadTon; }

    @Override public String toString() {
        return placa + "  |  " + conductor + "  |  " + capacidadTon + " t";
    }
}

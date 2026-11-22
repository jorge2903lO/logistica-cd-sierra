package logistica;

/** Artículo identificado por su código en el ABB. */
public class Producto {
    private int codigo;
    private String nombre;
    private int stock;

    public Producto(int codigo, String nombre, int stock) {
        setCodigo(codigo);
        setNombre(nombre);
        setStock(stock);
    }

    public int getCodigo() { return codigo; }

    public void setCodigo(int codigo) {
        if (codigo <= 0) throw new IllegalArgumentException("Valor inválido para codigo.");
        this.codigo = codigo;
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Valor inválido para nombre.");
        this.nombre = nombre;
    }

    public int getStock() { return stock; }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Valor inválido para stock.");
        this.stock = stock;
    }

    @Override public String toString() {
        return codigo + "  |  " + nombre + "  |  " + stock + " unidades";
    }
}

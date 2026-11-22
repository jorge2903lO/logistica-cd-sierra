package logistica;

/** Enlace del inventario, ordenado por código de producto. */
public class NodoArbol {
    final Producto producto;
    NodoArbol izquierdo, derecho;
    public NodoArbol(Producto producto) { this.producto = producto; }
}

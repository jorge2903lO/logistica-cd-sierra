package logistica;

import java.util.List;

/** ABB manual. La altura se expresa en niveles (árbol vacío = 0). */
public class ArbolInventario {
    private NodoArbol raiz;

    public void insertar(Producto producto) {
        if (raiz == null) { raiz = new NodoArbol(producto); return; }
        NodoArbol actual = raiz;
        while (true) {
            int orden = Integer.compare(producto.getCodigo(), actual.producto.getCodigo());
            if (orden == 0) throw new IllegalArgumentException("El código ya está registrado.");
            if (orden < 0) {
                if (actual.izquierdo == null) { actual.izquierdo = new NodoArbol(producto); return; }
                actual = actual.izquierdo;
            } else {
                if (actual.derecho == null) { actual.derecho = new NodoArbol(producto); return; }
                actual = actual.derecho;
            }
        }
    }

    public Producto buscar(int codigo) {
        NodoArbol actual = raiz;
        while (actual != null) {
            int clave = actual.producto.getCodigo();
            if (codigo == clave) return actual.producto;
            actual = codigo < clave ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    public void inOrden(List<Producto> salida) {
        visitar(raiz, salida);
    }

    private void visitar(NodoArbol nodo, List<Producto> salida) {
        if (nodo == null) return;
        visitar(nodo.izquierdo, salida);
        salida.add(nodo.producto);
        visitar(nodo.derecho, salida);
    }

    public int altura() { return altura(raiz); }

    private int altura(NodoArbol nodo) {
        if (nodo == null) return 0;
        return 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    public void descontarStock(int codigo, int cantidad) {
        Producto p = buscar(codigo);
        if (p == null) throw new IllegalArgumentException("Producto inexistente.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (cantidad > p.getStock()) throw new IllegalStateException("Stock insuficiente. Disponibles: " + p.getStock() + " unidades.");
        p.setStock(p.getStock() - cantidad);
    }
}

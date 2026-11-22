package logistica;

/** Datos ficticios del centro para reproducir la demostración. */
final class DatosEjemplo {
    private DatosEjemplo() {}
    static void cargar(ArbolInventario inventario, ColaCamiones cola, GrafoRutas red) {
        inventario.insertar(new Producto(520, "Quinua blanca", 72));
        inventario.insertar(new Producto(735, "Maíz amiláceo", 55));
        inventario.insertar(new Producto(280, "Habas secas", 110));
        inventario.insertar(new Producto(395, "Kiwicha", 46));
        inventario.insertar(new Producto(610, "Cebada perlada", 81));
        cola.encolar(new Camion("WAS-410", "Luz Ríos", 7));
        cola.encolar(new Camion("WAS-422", "Omar Soto", 5.5));
        cola.encolar(new Camion("WAS-435", "Sara Peña", 9));
        red.agregarCiudad("Huancayo");
        red.agregarCiudad("Jauja");
        red.agregarCiudad("Concepción");
        red.agregarCiudad("Tarma");
        red.agregarCiudad("La Oroya");
        red.agregarRuta("Huancayo", "Jauja", 45);
        red.agregarRuta("Huancayo", "Concepción", 22);
        red.agregarRuta("Jauja", "Tarma", 54);
        red.agregarRuta("Concepción", "La Oroya", 58);
    }
}

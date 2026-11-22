package logistica;

import java.util.*;

/** Red bidireccional. Cada tramo conserva su distancia y el orden de alta. */
public class GrafoRutas {
    private final HashMap<String, LinkedList<Ruta>> adyacencia = new HashMap<>();

    private String nombreRegistrado(String nombre) {
        for (String ciudad : adyacencia.keySet())
            if (ciudad.equalsIgnoreCase(nombre.trim())) return ciudad;
        throw new IllegalArgumentException("Ciudad no registrada: " + nombre);
    }

    public void agregarCiudad(String nombre) {
        nombre = nombre.trim();
        if (nombre.isEmpty()) throw new IllegalArgumentException("Ingrese la ciudad.");
        for (String ciudad : adyacencia.keySet())
            if (ciudad.equalsIgnoreCase(nombre)) throw new IllegalArgumentException("La ciudad ya existe.");
        adyacencia.put(nombre, new LinkedList<Ruta>());
    }

    public void agregarRuta(String origen, String destino, int km) {
        origen = nombreRegistrado(origen);
        destino = nombreRegistrado(destino);
        if (origen.equals(destino) || km <= 0) throw new IllegalArgumentException("Se requieren dos ciudades diferentes y una distancia positiva.");
        for (Ruta r : adyacencia.get(origen))
            if (r.getDestino().equals(destino)) throw new IllegalArgumentException("Ya existe este tramo.");
        adyacencia.get(origen).add(new Ruta(destino, km));
        adyacencia.get(destino).add(new Ruta(origen, km));
    }

    public String mostrarRutas() {
        StringBuilder texto = new StringBuilder();
        for (String ciudad : new TreeSet<>(adyacencia.keySet())) {
            StringJoiner tramos = new StringJoiner("  ·  ");
            for (Ruta r : adyacencia.get(ciudad)) tramos.add(r.toString());
            texto.append(ciudad).append("  →  ").append(tramos.length() == 0 ? "sin conexiones" : tramos).append("\n");
        }
        return texto.length() == 0 ? "Red vacía." : texto.toString().trim();
    }

    /** Marca las ciudades al encolarlas para visitarlas una sola vez. */
    public List<String> bfs(String inicio) {
        String primero = nombreRegistrado(inicio);
        LinkedList<String> orden = new LinkedList<>();
        ArrayDeque<String> pendientes = new ArrayDeque<>();
        Set<String> vistos = new HashSet<>();
        pendientes.add(primero);
        vistos.add(primero);
        while (!pendientes.isEmpty()) {
            String origen = pendientes.removeFirst();
            orden.add(origen);
            for (Ruta tramo : adyacencia.get(origen))
                if (vistos.add(tramo.getDestino())) pendientes.addLast(tramo.getDestino());
        }
        return orden;
    }

    /** Suma los kilómetros de un camino explícito, verificando cada salto. */
    public int distanciaRuta(List<String> camino) {
        int km = 0;
        String previo = null;
        for (String entrada : camino) {
            String ciudad = nombreRegistrado(entrada);
            if (previo != null) {
                Ruta encontrado = null;
                for (Ruta r : adyacencia.get(previo))
                    if (r.getDestino().equals(ciudad)) { encontrado = r; break; }
                if (encontrado == null) throw new IllegalArgumentException("El camino contiene un tramo inexistente.");
                km = Math.addExact(km, encontrado.getDistanciaKm());
            }
            previo = ciudad;
        }
        return km;
    }
}

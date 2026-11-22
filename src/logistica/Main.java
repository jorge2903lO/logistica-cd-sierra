package logistica;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Formulario del centro de distribución. Los botones delegan en estructuras separadas. */
public class Main extends JFrame {
    private final ArbolInventario alm = new ArbolInventario();
    private final ColaCamiones cola = new ColaCamiones();
    private final GrafoRutas redRutas = new GrafoRutas();
    private final ArrayList<Entrega> historial = new ArrayList<>();
    private final Color acento = Color.decode("#81532C");

    public Main() {
        initComponents();
        aplicarEstilo();
        conectarAcciones();
        DatosEjemplo.cargar(alm, cola, redRutas);
        mostrar("Resumen de operación", "CENTRO DE DISTRIBUCIÓN  /  SIERRA\n\n"
            + "Inventario inicial     5 productos\nVehículos en espera    3 camiones\n"
            + "Red de reparto         5 ciudades   ·   4 tramos\n\n"
            + "Seleccione un módulo para consultar o actualizar los datos de la sesión.");
        setSize(920, 620);
        setMinimumSize(new Dimension(760, 530));
        setLocationRelativeTo(null);
    }

    private void aplicarEstilo() {
        cabecera.setBorder(new EmptyBorder(12, 22, 12, 22));
        cabecera.setBackground(acento);
        identidad.setOpaque(false);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titulo.setForeground(Color.WHITE);
        subtitulo.setForeground(Color.WHITE);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cuerpo.setBorder(new EmptyBorder(14, 18, 14, 18));
        for (JPanel panel : new JPanel[]{inv, veh, red, env, his}) {
            panel.setBorder(new EmptyBorder(12, 12, 8, 12));
        }
        resultado.setBorder(new EmptyBorder(12, 0, 0, 0));
        seccion.setFont(new Font("Segoe UI", Font.BOLD, 17));
        seccion.setForeground(acento);
        seccion.setBorder(new EmptyBorder(0, 0, 8, 0));
        salida.setFont(new Font("Consolas", Font.PLAIN, 16));
        salida.setBackground(Color.WHITE);
        salida.setMargin(new Insets(15, 16, 15, 16));
        salida.setLineWrap(true);
        salida.setWrapStyleWord(true);
        guia.setBorder(new EmptyBorder(4, 2, 8, 2));
    }

    private void conectarAcciones() {
        accion(insertar, () -> {
            Producto p = new Producto(entero(codigo, "código"), texto(nombre, "producto"), entero(stock, "stock"));
            alm.insertar(p);
            mostrar("Producto incorporado", p.toString());
        });
        accion(buscar, () -> {
            int clave = entero(codigo, "código");
            Producto p = alm.buscar(clave);
            if (p == null) throw new IllegalArgumentException("No existe el producto " + clave + ".");
            mostrar("Resultado de búsqueda", "Código       " + clave + "\nProducto     " + p.getNombre() + "\nStock        " + p.getStock() + " unidades");
        });
        accion(ordenar, this::inventario);
        accion(encolar, () -> {
            String pat = texto(placa, "placa").toUpperCase(Locale.ROOT);
            String chofer = texto(conductor, "conductor");
            double toneladas;
            try { toneladas = Double.parseDouble(texto(capacidad, "capacidad").replace(',', '.')); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("La capacidad debe ser numérica."); }
            if (!Double.isFinite(toneladas) || toneladas <= 0) throw new IllegalArgumentException("La capacidad debe ser positiva.");
            if (cola.contienePlaca(pat)) throw new IllegalArgumentException("La placa ya está en cola.");
            cola.encolar(new Camion(pat, chofer, toneladas));
            mostrar("Camión encolado", cola.mostrar());
        });
        accion(desencolar, () -> mostrar("Salida de la cola", "Camión retirado: " + cola.desencolar() + "\n\nTurnos pendientes\n" + cola.mostrar()));
        accion(listarCola, () -> mostrar("Turnos de camiones", "POSICIÓN · PLACA · CONDUCTOR · CAPACIDAD\n\n" + cola.mostrar()));
        accion(nuevaCiudad, () -> {
            redRutas.agregarCiudad(texto(ciudad, "ciudad"));
            mostrar("Ciudad incorporada", redRutas.mostrarRutas());
        });
        accion(nuevaRuta, () -> {
            redRutas.agregarRuta(texto(origen, "origen"), texto(destinoRuta, "destino"), entero(km, "distancia"));
            mostrar("Ruta incorporada", redRutas.mostrarRutas());
        });
        accion(listarRutas, () -> mostrar("Red de reparto", "CIUDAD  →  CONEXIONES DIRECTAS Y DISTANCIA\n\n" + redRutas.mostrarRutas()));
        accion(recorrer, () -> {
            List<String> visita = redRutas.bfs(texto(ciudad, "ciudad de inicio"));
            mostrar("Recorrido en anchura (BFS)", "Origen          " + visita.get(0) + "\nCiudades        " + visita.size()
                + "\n\nOrden de visita\n" + String.join("  →  ", visita));
        });
        accion(registrar, this::registrarEntrega);
        accion(verHistorial, () -> {
            StringBuilder filas = new StringBuilder("N.º  |  PRODUCTO  |  UNIDADES  |  CAMIÓN  |  DESTINO\n\n");
            for (Entrega e : historial) filas.append(e).append("\n");
            mostrar("Historial de entregas", filas + "\nTotal de entregas: " + historial.size());
        });
        accion(salir, this::dispose);
    }

    /** Despacho atómico en el hilo de Swing. Si falla la red o la cola se restituye el stock. */
    private void registrarEntrega() {
        int clave = entero(codEnvio, "código de producto");
        int unidades = entero(cantidad, "unidades");
        String destino = texto(destinoEnvio, "destino");
        Producto p = alm.buscar(clave);
        if (p == null) throw new IllegalArgumentException("El producto no existe.");
        int anterior = p.getStock();
        alm.descontarStock(clave, unidades);
        Camion asignado;
        try {
            destino = redRutas.bfs(destino).get(0);
            asignado = cola.desencolar();
        } catch (IllegalArgumentException | IllegalStateException error) {
            p.setStock(anterior);
            throw error;
        }
        Entrega e = new Entrega(historial.size() + 1, p.getNombre(), asignado.getPlaca(), destino, unidades);
        historial.add(e);
        mostrar("Entrega registrada", e + "\n\nStock anterior     " + anterior + " u.\nStock disponible   " + p.getStock() + " u.");
        JOptionPane.showMessageDialog(this, "Entrega #" + e.getId() + " confirmada.\n" + unidades + " u. de " + p.getNombre()
                + " para " + destino + ".\nCamión asignado: " + asignado.getPlaca(), "Despacho confirmado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void inventario() {
        List<Producto> filas = new Vector<>(); // Solo salida del recorrido; el inventario permanece en los nodos.
        alm.inOrden(filas);
        StringBuilder tabla = new StringBuilder(String.format("%-10s%-28s%10s%n%n", "CÓDIGO", "PRODUCTO", "STOCK (u.)"));
        for (Producto p : filas) tabla.append(String.format("%-10d%-28s%10d%n", p.getCodigo(), p.getNombre(), p.getStock()));
        tabla.append("\nProductos: ").append(filas.size()).append("    ·    Altura del ABB: ").append(alm.altura());
        mostrar("Inventario ordenado por código", tabla.toString());
    }

    private void accion(JButton boton, Runnable trabajo) {
        boton.addActionListener(evento -> {
            try { trabajo.run(); }
            catch (IllegalArgumentException | IllegalStateException error) {
                mostrar("Operación rechazada", error.getMessage());
                JOptionPane.showMessageDialog(this, error.getMessage(), "Validación de la operación", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static String texto(JTextField campo, String etiqueta) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) throw new IllegalArgumentException("Complete el campo " + etiqueta + ".");
        return valor;
    }

    private static int entero(JTextField campo, String etiqueta) {
        try { return Integer.parseInt(texto(campo, etiqueta)); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Ingrese un número entero en " + etiqueta + "."); }
    }

    private void mostrar(String encabezado, String contenido) {
        seccion.setText(encabezado);
        salida.setText(contenido);
        salida.setCaretPosition(0);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        cabecera = new javax.swing.JPanel();
        identidad = new javax.swing.JPanel();
        titulo = new javax.swing.JLabel();
        subtitulo = new javax.swing.JLabel();
        salir = new javax.swing.JButton();
        cuerpo = new javax.swing.JPanel();
        modulos = new javax.swing.JTabbedPane();
        inv = new javax.swing.JPanel();
        datosinv = new javax.swing.JPanel();
        lbcodigo = new javax.swing.JLabel();
        lbnombre = new javax.swing.JLabel();
        lbstock = new javax.swing.JLabel();
        codigo = new javax.swing.JTextField();
        nombre = new javax.swing.JTextField();
        stock = new javax.swing.JTextField();
        accionesinv = new javax.swing.JPanel();
        insertar = new javax.swing.JButton();
        buscar = new javax.swing.JButton();
        ordenar = new javax.swing.JButton();
        veh = new javax.swing.JPanel();
        datosveh = new javax.swing.JPanel();
        lbplaca = new javax.swing.JLabel();
        lbconductor = new javax.swing.JLabel();
        lbcapacidad = new javax.swing.JLabel();
        placa = new javax.swing.JTextField();
        conductor = new javax.swing.JTextField();
        capacidad = new javax.swing.JTextField();
        accionesveh = new javax.swing.JPanel();
        encolar = new javax.swing.JButton();
        desencolar = new javax.swing.JButton();
        listarCola = new javax.swing.JButton();
        red = new javax.swing.JPanel();
        datosred = new javax.swing.JPanel();
        lbciudad = new javax.swing.JLabel();
        lborigen = new javax.swing.JLabel();
        lbdestinoRuta = new javax.swing.JLabel();
        lbkm = new javax.swing.JLabel();
        ciudad = new javax.swing.JTextField();
        origen = new javax.swing.JTextField();
        destinoRuta = new javax.swing.JTextField();
        km = new javax.swing.JTextField();
        accionesred = new javax.swing.JPanel();
        nuevaCiudad = new javax.swing.JButton();
        nuevaRuta = new javax.swing.JButton();
        listarRutas = new javax.swing.JButton();
        recorrer = new javax.swing.JButton();
        env = new javax.swing.JPanel();
        datosenv = new javax.swing.JPanel();
        lbcodEnvio = new javax.swing.JLabel();
        lbcantidad = new javax.swing.JLabel();
        lbdestinoEnvio = new javax.swing.JLabel();
        codEnvio = new javax.swing.JTextField();
        cantidad = new javax.swing.JTextField();
        destinoEnvio = new javax.swing.JTextField();
        accionesenv = new javax.swing.JPanel();
        registrar = new javax.swing.JButton();
        his = new javax.swing.JPanel();
        guia = new javax.swing.JLabel();
        accioneshis = new javax.swing.JPanel();
        verHistorial = new javax.swing.JButton();
        resultado = new javax.swing.JPanel();
        seccion = new javax.swing.JLabel();
        visor = new javax.swing.JScrollPane();
        salida = new javax.swing.JTextArea();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Central Andina");
        getContentPane().setLayout(new java.awt.BorderLayout());
        cabecera.setLayout(new java.awt.BorderLayout());
        identidad.setLayout(new java.awt.GridLayout(2, 1, 10, 4));
        titulo.setText("Central Andina");
        subtitulo.setText("Distribución de granos y cereales");
        salir.setText("Salir");
        cuerpo.setLayout(new java.awt.BorderLayout());
        modulos.setTabPlacement(2);
        inv.setLayout(new java.awt.BorderLayout());
        datosinv.setLayout(new java.awt.GridLayout(2, 3, 10, 4));
        lbcodigo.setText("Código");
        lbnombre.setText("Producto");
        lbstock.setText("Stock (unidades)");
        accionesinv.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        insertar.setText("Insertar producto");
        buscar.setText("Buscar por código");
        ordenar.setText("Mostrar inventario");
        veh.setLayout(new java.awt.BorderLayout());
        datosveh.setLayout(new java.awt.GridLayout(2, 3, 10, 4));
        lbplaca.setText("Placa");
        lbconductor.setText("Conductor");
        lbcapacidad.setText("Capacidad (t)");
        accionesveh.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        encolar.setText("Encolar");
        desencolar.setText("Desencolar");
        listarCola.setText("Mostrar cola");
        red.setLayout(new java.awt.BorderLayout());
        datosred.setLayout(new java.awt.GridLayout(2, 4, 10, 4));
        lbciudad.setText("Ciudad / inicio BFS");
        lborigen.setText("Origen del tramo");
        lbdestinoRuta.setText("Destino del tramo");
        lbkm.setText("Distancia (km)");
        accionesred.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        nuevaCiudad.setText("Agregar ciudad");
        nuevaRuta.setText("Agregar ruta");
        listarRutas.setText("Mostrar rutas");
        recorrer.setText("Recorrer BFS");
        env.setLayout(new java.awt.BorderLayout());
        datosenv.setLayout(new java.awt.GridLayout(2, 3, 10, 4));
        lbcodEnvio.setText("Código de producto");
        lbcantidad.setText("Unidades");
        lbdestinoEnvio.setText("Ciudad de destino");
        accionesenv.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        registrar.setText("Registrar entrega");
        his.setLayout(new java.awt.BorderLayout());
        guia.setText("Consulte los despachos registrados durante esta sesión.");
        accioneshis.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        verHistorial.setText("Consultar historial");
        resultado.setLayout(new java.awt.BorderLayout());
        seccion.setText("Resumen de operación");
        salida.setEditable(false);
        getContentPane().add(cabecera, java.awt.BorderLayout.NORTH);
        cabecera.add(identidad, java.awt.BorderLayout.CENTER);
        identidad.add(titulo);
        identidad.add(subtitulo);
        cabecera.add(salir, java.awt.BorderLayout.EAST);
        getContentPane().add(cuerpo, java.awt.BorderLayout.CENTER);
        cuerpo.add(modulos, java.awt.BorderLayout.NORTH);
        modulos.addTab("Almacén", inv);
        inv.add(datosinv, java.awt.BorderLayout.CENTER);
        datosinv.add(lbcodigo);
        datosinv.add(lbnombre);
        datosinv.add(lbstock);
        datosinv.add(codigo);
        datosinv.add(nombre);
        datosinv.add(stock);
        inv.add(accionesinv, java.awt.BorderLayout.SOUTH);
        accionesinv.add(insertar);
        accionesinv.add(buscar);
        accionesinv.add(ordenar);
        modulos.addTab("Turnos de carga", veh);
        veh.add(datosveh, java.awt.BorderLayout.CENTER);
        datosveh.add(lbplaca);
        datosveh.add(lbconductor);
        datosveh.add(lbcapacidad);
        datosveh.add(placa);
        datosveh.add(conductor);
        datosveh.add(capacidad);
        veh.add(accionesveh, java.awt.BorderLayout.SOUTH);
        accionesveh.add(encolar);
        accionesveh.add(desencolar);
        accionesveh.add(listarCola);
        modulos.addTab("Corredores", red);
        red.add(datosred, java.awt.BorderLayout.CENTER);
        datosred.add(lbciudad);
        datosred.add(lborigen);
        datosred.add(lbdestinoRuta);
        datosred.add(lbkm);
        datosred.add(ciudad);
        datosred.add(origen);
        datosred.add(destinoRuta);
        datosred.add(km);
        red.add(accionesred, java.awt.BorderLayout.SOUTH);
        accionesred.add(nuevaCiudad);
        accionesred.add(nuevaRuta);
        accionesred.add(listarRutas);
        accionesred.add(recorrer);
        modulos.addTab("Guías de salida", env);
        env.add(datosenv, java.awt.BorderLayout.CENTER);
        datosenv.add(lbcodEnvio);
        datosenv.add(lbcantidad);
        datosenv.add(lbdestinoEnvio);
        datosenv.add(codEnvio);
        datosenv.add(cantidad);
        datosenv.add(destinoEnvio);
        env.add(accionesenv, java.awt.BorderLayout.SOUTH);
        accionesenv.add(registrar);
        modulos.addTab("Libro de guías", his);
        his.add(guia, java.awt.BorderLayout.CENTER);
        his.add(accioneshis, java.awt.BorderLayout.SOUTH);
        accioneshis.add(verHistorial);
        cuerpo.add(resultado, java.awt.BorderLayout.CENTER);
        resultado.add(seccion, java.awt.BorderLayout.NORTH);
        resultado.add(visor, java.awt.BorderLayout.CENTER);
        visor.setViewportView(salida);
        pack();
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cabecera;
    private javax.swing.JPanel identidad;
    private javax.swing.JLabel titulo;
    private javax.swing.JLabel subtitulo;
    private javax.swing.JButton salir;
    private javax.swing.JPanel cuerpo;
    private javax.swing.JTabbedPane modulos;
    private javax.swing.JPanel inv;
    private javax.swing.JPanel datosinv;
    private javax.swing.JLabel lbcodigo;
    private javax.swing.JLabel lbnombre;
    private javax.swing.JLabel lbstock;
    private javax.swing.JTextField codigo;
    private javax.swing.JTextField nombre;
    private javax.swing.JTextField stock;
    private javax.swing.JPanel accionesinv;
    private javax.swing.JButton insertar;
    private javax.swing.JButton buscar;
    private javax.swing.JButton ordenar;
    private javax.swing.JPanel veh;
    private javax.swing.JPanel datosveh;
    private javax.swing.JLabel lbplaca;
    private javax.swing.JLabel lbconductor;
    private javax.swing.JLabel lbcapacidad;
    private javax.swing.JTextField placa;
    private javax.swing.JTextField conductor;
    private javax.swing.JTextField capacidad;
    private javax.swing.JPanel accionesveh;
    private javax.swing.JButton encolar;
    private javax.swing.JButton desencolar;
    private javax.swing.JButton listarCola;
    private javax.swing.JPanel red;
    private javax.swing.JPanel datosred;
    private javax.swing.JLabel lbciudad;
    private javax.swing.JLabel lborigen;
    private javax.swing.JLabel lbdestinoRuta;
    private javax.swing.JLabel lbkm;
    private javax.swing.JTextField ciudad;
    private javax.swing.JTextField origen;
    private javax.swing.JTextField destinoRuta;
    private javax.swing.JTextField km;
    private javax.swing.JPanel accionesred;
    private javax.swing.JButton nuevaCiudad;
    private javax.swing.JButton nuevaRuta;
    private javax.swing.JButton listarRutas;
    private javax.swing.JButton recorrer;
    private javax.swing.JPanel env;
    private javax.swing.JPanel datosenv;
    private javax.swing.JLabel lbcodEnvio;
    private javax.swing.JLabel lbcantidad;
    private javax.swing.JLabel lbdestinoEnvio;
    private javax.swing.JTextField codEnvio;
    private javax.swing.JTextField cantidad;
    private javax.swing.JTextField destinoEnvio;
    private javax.swing.JPanel accionesenv;
    private javax.swing.JButton registrar;
    private javax.swing.JPanel his;
    private javax.swing.JLabel guia;
    private javax.swing.JPanel accioneshis;
    private javax.swing.JButton verHistorial;
    private javax.swing.JPanel resultado;
    private javax.swing.JLabel seccion;
    private javax.swing.JScrollPane visor;
    private javax.swing.JTextArea salida;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* Se conserva la apariencia Swing disponible. */ }
            new Main().setVisible(true);
        });
    }
}

package servidorswing;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class ServidorSwing1 extends javax.swing.JFrame {

    private JTextArea txtLog;
    private JButton btnIniciar;

    private ServerSocket servidor;
    private boolean activo = false;
    private Map<String, ManejadorCliente> clientes = new HashMap<>();

    private final int PUERTO = 59420;

    public ServidorSwing1() {
        initComponents();
        setTitle("Log del Servidor");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Log del Servidor", SwingConstants.CENTER);
        titulo.setOpaque(true);
        titulo.setBackground(new Color(0, 170, 70));
        titulo.setForeground(Color.YELLOW);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setPreferredSize(new Dimension(500, 40));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(txtLog);

        btnIniciar = new JButton("Iniciar Servidor");

        add(titulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btnIniciar, BorderLayout.SOUTH);

        btnIniciar.addActionListener(e -> iniciarServidor());
    }

    private void iniciarServidor() {
        if (activo) {
            return;
        }

        activo = true;
        btnIniciar.setEnabled(false);

        Thread hiloServidor = new Thread(() -> {
            try {
                servidor = new ServerSocket(PUERTO);
                agregarLog("Servidor iniciado y contestando OK");
                agregarLog("Puerto: " + PUERTO);

                while (activo) {
                    Socket socketCliente = servidor.accept();

                    ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                    manejador.start();
                }

            } catch (IOException e) {
                agregarLog("Error en servidor: " + e.getMessage());
            }
        });

        hiloServidor.start();
    }

    private void agregarLog(String texto) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(texto + "\n");
        });
    }

    private synchronized void agregarCliente(String nombre, ManejadorCliente cliente) {
        clientes.put(nombre, cliente);
        agregarLog("Usuario \"" + nombre + "\" conectado");
        enviarListaUsuariosATodos();
    }

    private synchronized void eliminarCliente(String nombre) {
        clientes.remove(nombre);
        agregarLog("El usuario \"" + nombre + "\" abandonó");
        enviarListaUsuariosATodos();
    }

    private synchronized void enviarListaUsuariosATodos() {
        StringBuilder lista = new StringBuilder();

        lista.append("LISTA_USUARIOS:");

        for (String usuario : clientes.keySet()) {
            lista.append(usuario).append(",");
        }

        for (ManejadorCliente cliente : clientes.values()) {
            cliente.enviar(lista.toString());
        }
    }

    private synchronized void reenviarMensaje(String remitente, String destinatario, String mensaje) {
        ManejadorCliente clienteDestino = clientes.get(destinatario);
        ManejadorCliente clienteRemitente = clientes.get(remitente);

        if (clienteDestino != null) {
            clienteDestino.enviar("MENSAJE:" + remitente + "--> " + mensaje);
            clienteRemitente.enviar("MENSAJE:" + remitente + "--> " + mensaje);

            agregarLog(remitente + " envió mensaje a " + destinatario);
        } else {
            clienteRemitente.enviar("MENSAJE:El usuario " + destinatario + " no está conectado.");
        }
    }

    class ManejadorCliente extends Thread {

        private Socket socket;
        private BufferedReader entrada;
        private PrintWriter salida;
        private String nombreUsuario;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                nombreUsuario = entrada.readLine();

                agregarCliente(nombreUsuario, this);

                String linea;

                while ((linea = entrada.readLine()) != null) {

                    if (linea.equalsIgnoreCase("CHAO")) {
                        break;
                    }

                    if (linea.startsWith("ENVIAR:")) {
                        String contenido = linea.substring(7);

                        String[] partes = contenido.split(":", 2);

                        if (partes.length == 2) {
                            String destinatario = partes[0];
                            String mensaje = partes[1];

                            reenviarMensaje(nombreUsuario, destinatario, mensaje);
                        }
                    }
                }

            } catch (IOException e) {
                agregarLog("Error con usuario: " + nombreUsuario);
            } finally {
                cerrarConexion();
            }
        }

        public void enviar(String mensaje) {
            salida.println(mensaje);
        }

        private void cerrarConexion() {
            try {
                eliminarCliente(nombreUsuario);

                if (socket != null) {
                    socket.close();
                }

            } catch (IOException e) {
                agregarLog("Error cerrando conexión de " + nombreUsuario);
            }
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServidorSwing1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServidorSwing1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServidorSwing1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServidorSwing1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServidorSwing1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

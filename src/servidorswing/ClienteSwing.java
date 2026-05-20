
package servidorswing;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 *
 * @author User
 */
public class ClienteSwing extends javax.swing.JFrame {

   private JTextField txtIp;
    private JTextField txtPuerto;
    private JTextField txtNombre;
    private JButton btnConectar;

    private JComboBox<String> comboUsuarios;
    private JTextArea txtChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnSalir;

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    private String nombreUsuario;
    public ClienteSwing() {
        initComponents();
        setTitle("Cliente Chat");
        setSize(550, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        crearPanelConexion();
        crearPanelChat();

        btnConectar.addActionListener(e -> conectarServidor());
        btnEnviar.addActionListener(e -> enviarMensaje());
        btnSalir.addActionListener(e -> salirChat());
    }

    private void crearPanelConexion() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de conexión"));

        JLabel lblIp = new JLabel("IP del Servidor:");
        JLabel lblPuerto = new JLabel("Puerto:");
        JLabel lblNombre = new JLabel("Nombre de usuario:");

        txtIp = new JTextField("127.0.0.1");
        txtPuerto = new JTextField("59420");
        txtNombre = new JTextField("poli01");

        btnConectar = new JButton("Conectar");

        panel.add(lblIp);
        panel.add(txtIp);

        panel.add(lblPuerto);
        panel.add(txtPuerto);

        panel.add(lblNombre);
        panel.add(txtNombre);

        panel.add(new JLabel(""));
        panel.add(btnConectar);

        add(panel, BorderLayout.NORTH);
    }

    private void crearPanelChat() {
        JPanel panelCentral = new JPanel(new BorderLayout());

        JPanel panelUsuario = new JPanel(new BorderLayout());
        panelUsuario.setBorder(BorderFactory.createTitledBorder("Usuario para conversar"));

        comboUsuarios = new JComboBox<>();
        panelUsuario.add(comboUsuarios, BorderLayout.CENTER);

        txtChat = new JTextArea();
        txtChat.setEditable(false);
        txtChat.setFont(new Font("Consolas", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(txtChat);

        JPanel panelMensaje = new JPanel(new BorderLayout());

        txtMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnSalir = new JButton("Chao");

        JPanel panelBotones = new JPanel(new GridLayout(1, 2));
        panelBotones.add(btnEnviar);
        panelBotones.add(btnSalir);

        panelMensaje.add(txtMensaje, BorderLayout.CENTER);
        panelMensaje.add(panelBotones, BorderLayout.EAST);

        panelCentral.add(panelUsuario, BorderLayout.NORTH);
        panelCentral.add(scroll, BorderLayout.CENTER);
        panelCentral.add(panelMensaje, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        habilitarChat(false);
    }

    private void conectarServidor() {
        try {
            String ip = txtIp.getText();
            int puerto = Integer.parseInt(txtPuerto.getText());
            nombreUsuario = txtNombre.getText();

            socket = new Socket(ip, puerto);

            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            salida.println(nombreUsuario);

            txtChat.append("Conectado al servidor como: " + nombreUsuario + "\n");

            habilitarConexion(false);
            habilitarChat(true);

            Thread hiloEscucha = new Thread(() -> escucharServidor());
            hiloEscucha.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar: " + e.getMessage());
        }
    }

    private void escucharServidor() {
        try {
            String linea;

            while ((linea = entrada.readLine()) != null) {

                if (linea.startsWith("LISTA_USUARIOS:")) {
                    actualizarUsuarios(linea);
                }

                if (linea.startsWith("MENSAJE:")) {
                    String mensaje = linea.substring(8);

                    SwingUtilities.invokeLater(() -> {
                        txtChat.append(mensaje + "\n");
                    });
                }
            }

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                txtChat.append("Conexión finalizada.\n");
            });
        }
    }

    private void actualizarUsuarios(String linea) {
        SwingUtilities.invokeLater(() -> {
            comboUsuarios.removeAllItems();

            String usuarios = linea.substring(15);

            String[] lista = usuarios.split(",");

            for (String usuario : lista) {
                if (!usuario.isEmpty() && !usuario.equals(nombreUsuario)) {
                    comboUsuarios.addItem(usuario);
                }
            }
        });
    }

    private void enviarMensaje() {
        String destinatario = (String) comboUsuarios.getSelectedItem();
        String mensaje = txtMensaje.getText();

        if (destinatario == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario.");
            return;
        }

        if (mensaje.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe escribir un mensaje.");
            return;
        }

        salida.println("ENVIAR:" + destinatario + ":" + mensaje);
        txtMensaje.setText("");
    }

    private void salirChat() {
        try {
            if (salida != null) {
                salida.println("CHAO");
            }

            if (socket != null) {
                socket.close();
            }

            txtChat.append("Usted abandonó el chat.\n");

            habilitarChat(false);
            habilitarConexion(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al salir.");
        }
    }

    private void habilitarConexion(boolean estado) {
        txtIp.setEnabled(estado);
        txtPuerto.setEnabled(estado);
        txtNombre.setEnabled(estado);
        btnConectar.setEnabled(estado);
    }

    private void habilitarChat(boolean estado) {
        comboUsuarios.setEnabled(estado);
        txtMensaje.setEnabled(estado);
        btnEnviar.setEnabled(estado);
        btnSalir.setEnabled(estado);
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

    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(ClienteSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClienteSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClienteSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClienteSwing.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClienteSwing().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

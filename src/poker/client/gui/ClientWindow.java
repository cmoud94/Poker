package poker.client.gui;

import javax.swing.*;
import java.awt.*;

public class ClientWindow {

    private static ClientWindow window;

    private JFrame frame;

    private Container contentPane;

    private Insets insets;

    private ConnectionPanel connectionPanel;

    private GamePanel gamePanel;

    public ClientWindow() {
        initialize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    window = new ClientWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Poker - \u00a9 2015 cmoud94\u2122");
        frame.setResizable(false);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        contentPane = frame.getContentPane();
        contentPane.setLayout(null);
        insets = contentPane.getInsets();

        connectionPanel = new ConnectionPanel();
        connectionPanel.setSize(150, contentPane.getHeight());
        contentPane.add(connectionPanel);
    }

}

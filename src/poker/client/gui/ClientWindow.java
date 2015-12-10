package poker.client.gui;

import poker.client.Client;

import javax.swing.*;
import java.awt.*;

public class ClientWindow {

    private static ClientWindow window;

    private JFrame frame;

    private Container contentPane;

    private Insets insets;

    private ConnectionPanel connectionPanel;

    private GamePanel gamePanel;

    private final Client client;

    public ClientWindow(Client client) {
        this.client = client;
        initialize();
    }

    public ConnectionPanel getConnectionPanel() {
        return connectionPanel;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    window = new ClientWindow(new Client());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame();
        frame.setTitle("Poker - \u00a9 2015 cmoud94\u2122");
        frame.setResizable(false);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        contentPane = frame.getContentPane();
        contentPane.setLayout(null);
        insets = contentPane.getInsets();

        connectionPanel = new ConnectionPanel(insets.left, insets.top, 200, contentPane.getHeight(), client);
        contentPane.add(connectionPanel);

        gamePanel = new GamePanel(connectionPanel.getWidth(), insets.top, 600, contentPane.getHeight(), client);
        contentPane.add(gamePanel);
    }

}

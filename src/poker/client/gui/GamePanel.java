package poker.client.gui;

import poker.client.Client;

import javax.swing.*;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Client client;

    public GamePanel(int x, int y, int width, int height, Client client) {
        this.client = client;
        this.setBounds(x, y, width, height);
        this.initComponents();
    }

    private void initComponents() {

    }

}

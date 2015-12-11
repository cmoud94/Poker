/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client.gui;

import poker.client.Client;

import javax.swing.*;
import java.awt.*;

public class ClientWindow {

    private static ClientWindow window;

    private ConnectionPanel connectionPanel;

    private GamePanel gamePanel;

    private final Client client;

    private final int WIDTH = 1000;

    private final int HEIGHT = 600;

    public ClientWindow() {
        this.client = new Client(this);
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
                    window = new ClientWindow();
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

        JFrame frame = new JFrame();
        frame.setTitle("Poker - \u00a9 2015 cmoud94\u2122");
        frame.setResizable(false);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(null);
        Insets insets = contentPane.getInsets();

        connectionPanel = new ConnectionPanel(insets.left, insets.top, (WIDTH / 5), contentPane.getHeight(), client);
        contentPane.add(connectionPanel);

        gamePanel = new GamePanel(connectionPanel.getWidth(), insets.top, (contentPane.getWidth() - connectionPanel.getWidth()), contentPane.getHeight(), client);
        contentPane.add(gamePanel);
    }

}

package poker.client.gui;

import poker.client.Client;

import javax.swing.*;
import java.awt.*;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */


public class MainWindow {

    private static MainWindow window;

    private static Container pane;

    private static Insets insets;

    private static ConnectionPanel connectionPanel;

    private static GamePanel gamePanel;

    private Client client;

    private MainWindow() {
        this.client = new Client();
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        window = new MainWindow();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Poker - \u00a9 2015 cmoud94\u2122");
        frame.setBounds(100, 100, 800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pane = frame.getContentPane();
        //pane.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        pane.setLayout(null);
        insets = pane.getInsets();

        initComponents();

        frame.setVisible(true);
    }

    private static void initComponents() {
        connectionPanel = new ConnectionPanel(insets.left, insets.top, 150, pane.getHeight());
        pane.add(connectionPanel);

        /*gamePanel = new GamePanel(connectionPanel.getWidth(), insets.top, (pane.getWidth() - connectionPanel.getWidth()), pane.getHeight());
        pane.add(gamePanel);*/
    }

}

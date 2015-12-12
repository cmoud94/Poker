/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client.gui;

import poker.client.Client;
import poker.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static ClientWindow parent;

    private static Client client;

    private static JTextField textFieldAddress;

    private static JTextField textFieldName;

    private static JButton buttonConnect;

    private static JLabel labelConnectionInfo;

    private static JLabel labelPlayerReady;

    private static JButton buttonReady;

    public ConnectionPanel(ClientWindow parent, int x, int y, int width, int height, Client client) {
        ConnectionPanel.parent = parent;
        ConnectionPanel.client = client;

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.BLUE));

        this.initComponents();
    }

    private static Client getClient() {
        return client;
    }

    public void serverReady() {
        labelPlayerReady.setVisible(true);
        buttonReady.setVisible(true);
    }

    public void clientConnected() {
        buttonConnect.setText("Disconnect");
        labelConnectionInfo.setText(getClient().getName() + "@" + getClient().getAddress() + ":" + getClient().getPort());
    }

    public void clientDisconnected() {
        labelConnectionInfo.setText("Offline");
        buttonConnect.setText("Connect");
    }

    private void initComponents() {
        int itemWidth = 180;
        int itemHeight = 30;
        int posY = this.getInsets().top;

        JLabel labelAddress = new JLabel("Address:port");
        labelAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(labelAddress);
        posY += itemHeight;

        textFieldAddress = new JTextField();
        textFieldAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(textFieldAddress);
        posY += itemHeight;

        //TODO: Smazat!!!
        textFieldAddress.setText("localhost:9999");

        JLabel labelName = new JLabel("Name");
        labelName.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(labelName);
        posY += itemHeight;

        textFieldName = new JTextField();
        textFieldName.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(textFieldName);
        posY += itemHeight;

        buttonConnect = new JButton("Connect");
        buttonConnect.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(buttonConnect);
        posY += itemHeight;
        buttonConnect.addActionListener(new buttonConnectAction());

        JLabel labelStatus = new JLabel("Connection status", JLabel.CENTER);
        labelStatus.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(labelStatus);
        posY += itemHeight;

        labelConnectionInfo = new JLabel("", JLabel.CENTER);
        labelConnectionInfo.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(labelConnectionInfo);
        posY += itemHeight;

        labelPlayerReady = new JLabel("Are you ready?", JLabel.CENTER);
        labelPlayerReady.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelPlayerReady.setVisible(false);
        this.add(labelPlayerReady);
        posY += itemHeight;

        buttonReady = new JButton("Ready");
        buttonReady.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        buttonReady.setVisible(false);
        this.add(buttonReady);
        buttonReady.addActionListener(new buttonReadyAction());
    }

    private static class buttonConnectAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!textFieldAddress.getText().equals("") && !textFieldName.getText().equals("") && buttonConnect.getText().equals("Connect")) {
                System.out.println("[ConnectionPanel - buttonConnectAction] Connecting to server...");

                String address = textFieldAddress.getText().trim();
                int port = Integer.parseInt(address.substring((address.indexOf(':') + 1), address.length()));
                address = address.substring(0, address.indexOf(':'));

                getClient().setName(textFieldName.getText().trim());
                getClient().setAddress(address);
                getClient().setPort(port);
                getClient().init();

                System.out.println("\tAddress: " + address + " port: " + port);
                System.out.println("\tPlayer name: " + getClient().getName());

                Thread clientLoop = new Thread(client, "clientLoop");
                clientLoop.start();

                if (clientLoop.isAlive()) {
                    System.out.println("[Thread] ClientLoop is alive");
                }
                System.out.println("[ConnectionPanel] Connection finished");
            } else if (buttonConnect.getText().equals("Disconnect")) {
                getClient().closeConnection();
                buttonConnect.setText("Connect");
                labelConnectionInfo.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Input the name and address in the right format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class buttonReadyAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            System.out.println("[ConnectionPanel] Ready to play");
            labelPlayerReady.setVisible(false);
            buttonReady.setVisible(false);
            getClient().sendData(Utils.getObjectAsBytes("yes"));
        }
    }

}

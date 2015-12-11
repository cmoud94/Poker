package poker.client.gui;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import poker.client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static Client client;

    private static JTextField textFieldAddress;

    private static JTextField textFieldName;

    private static JButton buttonConnect;

    public ConnectionPanel(int x, int y, int width, int height, Client client) {
        ConnectionPanel.client = client;

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.BLUE));

        this.initComponents();
    }

    private static Client getClient() {
        return client;
    }

    private void initComponents() {
        int itemWidth = 180;
        int itemHeight = 30;
        int posY = this.getInsets().top;

        JLabel labelAddress = new JLabel("Address:port");
        labelAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelAddress.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        this.add(labelAddress);
        posY += itemHeight;

        textFieldAddress = new JTextField();
        textFieldAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(textFieldAddress);
        posY += itemHeight;

        JLabel labelName = new JLabel("Name");
        labelName.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
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
        labelStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        this.add(labelStatus);
        posY += itemHeight;

        JLabel labelConnStatus = new JLabel("", JLabel.CENTER);
        labelConnStatus.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelConnStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        this.add(labelConnStatus);
    }

    private static class buttonConnectAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!textFieldAddress.getText().equals("") && !textFieldName.getText().equals("") && buttonConnect.getText().equals("Connect")) {
                System.out.println("[ConnectionPanel - buttonConnectAction] Connecting to server...");

                getClient().setName(textFieldName.getText().trim());

                String address = textFieldAddress.getText().trim();
                int port = Integer.parseInt(address.substring((address.indexOf(':') + 1), (address.length() - 1)));
                address = address.substring(0, address.indexOf(':'));

                System.out.println("\tAddress: " + address + " port: " + port);
                System.out.println("\tPlayer name: " + getClient().getName());

                getClient().connect(address, port);
                buttonConnect.setText("Disconnect");
            } else {
                JOptionPane.showMessageDialog(null, "Input the name and address in the right format.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            if (buttonConnect.getText().equals("Disconnect")) {
                getClient().disconnect();
                buttonConnect.setText("Connect");
            }
        }
    }

}

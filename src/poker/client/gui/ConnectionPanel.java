package poker.client.gui;

import poker.client.Client;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static Client client;

    private static JLabel labelAddress;

    private static JTextField textFieldAddress;

    private static JLabel labelName;

    private static JTextField textFieldName;

    private static JLabel labelStatus;

    private static JButton buttonConnect;

    private static JLabel labelConnStatus;

    public ConnectionPanel(int x, int y, int width, int height, Client client) {
        ConnectionPanel.client = client;

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        this.setBorder(new LineBorder(Color.BLUE));

        this.initComponents();
    }

    private static Client getClient() {
        return client;
    }

    private void initComponents() {
        int itemWidth = 180;
        int itemHeight = 30;
        int posY = this.getInsets().top;

        labelAddress = new JLabel("Address:port");
        labelAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelAddress.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        this.add(labelAddress);
        posY += itemHeight;

        textFieldAddress = new JTextField();
        textFieldAddress.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        this.add(textFieldAddress);
        posY += itemHeight;

        labelName = new JLabel("Name");
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

        labelStatus = new JLabel("Connection status", JLabel.CENTER);
        labelStatus.setBounds(this.getInsets().left + ((this.getWidth() - itemWidth) / 2), posY, itemWidth, itemHeight);
        labelStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        this.add(labelStatus);
        posY += itemHeight;

        labelConnStatus = new JLabel("", JLabel.CENTER);
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

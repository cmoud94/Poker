package poker.client.gui;

import poker.client.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Client client;

    private static JLabel background;

    public GamePanel(int x, int y, int width, int height, Client client) {
        this.client = client;

        this.setBounds(x, y, width, height);
        this.setLayout(null);

        this.initComponents();
    }

    public Client getClient() {
        return client;
    }

    private void initComponents() {
        BufferedImage bufferedImage = null;
        ImageIcon imageIcon = null;

        try {
            bufferedImage = ImageIO.read(new FileInputStream("poker/client/gui/img/table_1.png"));
            imageIcon = new ImageIcon(bufferedImage.getScaledInstance(this.getWidth(), this.getHeight(), BufferedImage.SCALE_DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
        }

        background = new JLabel(imageIcon);
        background.setBounds(this.getInsets().left, this.getInsets().top, this.getWidth(), this.getHeight());
        this.add(background);
    }

}

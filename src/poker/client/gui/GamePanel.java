package poker.client.gui;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import poker.client.Client;
import poker.game.Card;
import poker.game.Deck;
import poker.utils.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Client client;

    private static List<JLabel> buttons;

    // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
    private final int cardWidth = 50;

    private final int cardHeight = 72;

    public static GamePanel gamePanel;

    public GamePanel(int x, int y, int width, int height, Client client) {
        this.client = client;
        buttons = new ArrayList<>();
        gamePanel = this;

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.RED));

        this.initComponents(40);
    }

    public Client getClient() {
        return client;
    }

    public void setButtonPosition(String name, int posX, int posY) {
        for (JLabel button : buttons) {
            if (button.getName().equals(name)) {
                button.setLocation(posX - (button.getWidth() / 2), posY - (button.getHeight() / 2));
                button.setVisible(true);
            }
        }
    }

    private void initComponents(int chipSize) {
        buttons.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/dealer_button.png"), chipSize, chipSize)));
        buttons.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/small_blind.png"), chipSize, chipSize)));
        buttons.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/big_blind.png"), chipSize, chipSize)));
        buttons.get(0).setName("DEALER");
        buttons.get(1).setName("SMALL_BLIND");
        buttons.get(2).setName("BIG_BLIND");

        for (JLabel button : buttons) {
            button.setSize(chipSize, chipSize);
            button.setVisible(false);
            this.add(button);
        }

        JLabel background = new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/table_1.png"), this.getWidth(), this.getHeight()));
        background.setBounds(this.getInsets().left, this.getInsets().top, this.getWidth(), this.getHeight());
        this.add(background);
    }

    public void drawCommunityCards(List<Card> cards) {
        //List<JLabel> labels = new ArrayList<>();
        int communityCardsPosX = 250;
        int communityCardsPosY = 200;

        for (int i = 0; i < cards.size(); i++) {
            JLabel label = new JLabel(Utils.getScaledImageAsImageIcon(cards.get(i).getCardImage(), cardWidth, cardHeight));
            label.setBounds(i * cardWidth + communityCardsPosX + (i * 10), communityCardsPosY, cardWidth, cardHeight);
            //labels.add(label);
            this.add(label);
        }
    }

    public void drawAllCards() {
        //List<JLabel> cards = new ArrayList<>();
        Deck deck = new Deck();
        ImageIcon cardBackImage = null;

        for (int i = 0; i < Card.getSuits().length; i++) {
            for (int j = 0; j < Card.getRanks().length; j++) {
                Card card = deck.dealCard();
                JLabel label = new JLabel(Utils.getScaledImageAsImageIcon(card.getCardImage(), cardWidth, cardHeight));
                label.setBounds(j * cardWidth + 75, i * cardHeight + 100, cardWidth, cardHeight);
                //cards.add(label);
                this.add(label);

                if (cardBackImage == null) {
                    cardBackImage = Utils.getScaledImageAsImageIcon(card.getCardBackImage(), cardWidth, cardHeight);
                }
            }
        }

        JLabel cardBack = new JLabel(cardBackImage);
        cardBack.setBounds(75, 100 + 4 * cardHeight, cardWidth, cardHeight);
        this.add(cardBack);
    }

}

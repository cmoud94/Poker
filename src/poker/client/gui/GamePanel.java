/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client.gui;

import poker.client.Client;
import poker.game.Card;
import poker.game.Deck;
import poker.utils.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static Client client;

    private static List<JLabel> chips;

    private static JSlider slider;

    // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
    private final int cardWidth = 50;

    private final int cardHeight = 72;

    public GamePanel(int x, int y, int width, int height, Client client) {
        GamePanel.client = client;
        chips = new ArrayList<>();

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.RED));

        this.initComponents(40);
    }

    public static Client getClient() {
        return client;
    }

    public void setChipPosition(String name, int posX, int posY) {
        for (JLabel button : chips) {
            if (button.getName().equals(name)) {
                button.setLocation(posX - (button.getWidth() / 2), posY - (button.getHeight() / 2));
                button.setVisible(true);
            }
        }
    }

    private void initComponents(int chipSize) {
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/dealer_button.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/small_blind.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/big_blind.png"), chipSize, chipSize)));
        chips.get(0).setName("DEALER");
        chips.get(1).setName("SMALL_BLIND");
        chips.get(2).setName("BIG_BLIND");

        for (JLabel button : chips) {
            button.setSize(chipSize, chipSize);
            button.setVisible(false);
            this.add(button);
        }

        List<String> aa = new ArrayList<>();
        aa.add("fold");
        aa.add("call");
        aa.add("bet");
        aa.add("all-in");

        this.showPlayerAvailableAction(aa, 500);

        JLabel background = new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/table_1.png"), this.getWidth(), this.getHeight()));
        background.setBounds(this.getInsets().left, this.getInsets().top, this.getWidth(), this.getHeight());
        this.add(background);
    }

    public void showPlayerAvailableAction(List<String> availableActions, int betLimit) {
        int actionsPosX = 175;
        int actionsPosY = this.getHeight() - 100;
        int buttonWidth = 100;
        int buttonHeight = 30;

        for (int i = 0; i < availableActions.size(); i++) {
            JButton button = new JButton(availableActions.get(i));
            button.setActionCommand(button.getText());
            button.setBounds(i * buttonWidth + actionsPosX + (i * 10), actionsPosY, buttonWidth, buttonHeight);
            this.add(button);
            button.addActionListener(new buttonPlayerActionsListener());
        }

        slider = new JSlider(JSlider.HORIZONTAL, 10, betLimit, 10);
        slider.setBounds(this.getInsets().left, this.getHeight() - 60, this.getWidth(), 60);
        slider.setOpaque(true);
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        this.add(slider);
        slider.addChangeListener(new bettingSliderChangeListener());
    }

    public void drawCommunityCards(List<Card> cards) {
        int communityCardsPosX = 250;
        int communityCardsPosY = 200;

        for (int i = 0; i < cards.size(); i++) {
            JLabel label = new JLabel(Utils.getScaledImageAsImageIcon(cards.get(i).getCardImage(), cardWidth, cardHeight));
            label.setBounds(i * cardWidth + communityCardsPosX + (i * 10), communityCardsPosY, cardWidth, cardHeight);
            this.add(label);
        }
    }

    public void drawAllCards() {
        Deck deck = new Deck();
        ImageIcon cardBackImage = null;

        for (int i = 0; i < Card.getSuits().length; i++) {
            for (int j = 0; j < Card.getRanks().length; j++) {
                Card card = deck.dealCard();
                JLabel label = new JLabel(Utils.getScaledImageAsImageIcon(card.getCardImage(), cardWidth, cardHeight));
                label.setBounds(j * cardWidth + 75, i * cardHeight + 100, cardWidth, cardHeight);
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

    public static class buttonPlayerActionsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (actionEvent.getActionCommand()) {
                case "fold":
                    System.out.println("[GamePanel] fold triggered");
                    //getClient().sendMessage(Utils.getObjectAsBytes("fold"));
                    break;
                case "call":
                    System.out.println("[GamePanel] call triggered");
                    //getClient().sendMessage(Utils.getObjectAsBytes("call"));
                    break;
                case "bet":
                    System.out.println("[GamePanel] bet triggered with value of " + slider.getValue());
                    //getClient().sendMessage(Utils.getObjectAsBytes("bet" + String.valueOf(slider.getValue())));
                    break;
                case "all-in":
                    System.out.println("[GamePanel] all-in triggered");
                    //getClient().sendMessage(Utils.getObjectAsBytes("all-in"));
                    break;
                default:
                    break;
            }
        }
    }

    public static class bettingSliderChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if (slider.getValue() % 10 != 0) {
                slider.setValue(Math.round(slider.getValue() / 10) * 10);
            }
        }
    }

}

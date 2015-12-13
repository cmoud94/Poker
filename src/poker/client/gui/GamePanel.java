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
import poker.game.Player;
import poker.utils.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static ClientWindow parent;

    private static Client client;

    private static JSlider slider;

    // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
    private final int cardWidth = 50;

    private final int cardHeight = 72;

    private final int chipSize = 40;

    private static List<JButton> actionButtons;

    private final List<JPanel> playerPanels;

    public GamePanel(ClientWindow parent, int x, int y, int width, int height, Client client) {
        GamePanel.parent = parent;
        GamePanel.client = client;
        GamePanel.actionButtons = new ArrayList<>();
        this.playerPanels = new ArrayList<>();

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.RED));

        this.initComponents();
    }

    public static Client getClient() {
        return client;
    }

    public void showAvailableActions(ArrayList availableActions, int money) {
        slider.setMaximum(money - 10);
        for (JButton button : this.actionButtons) {
            if (availableActions.contains(button.getText())) {
                button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }
        }
    }

    public static void disableActions() {
        for (JButton button : GamePanel.actionButtons) {
            button.setEnabled(false);
        }
    }

    public void showPlayersInfo(Player player, boolean showCards) {
        if (!this.playerPanels.contains(player)) {
            PlayerPanel playerPanel = new PlayerPanel(this, player, (this.playerPanels.size() - 1), showCards, player.getName().equals(getClient().getName()));
            this.playerPanels.add(playerPanel);
            this.add(this.playerPanels.get(this.playerPanels.size() - 1));
        }
    }

    private void initComponents() {
        // Player panel
        /*Player player = new Player("Player", 1000);
        player.setBlind(Player.Blind.NO_BLIND);

        Deck deck = new Deck();
        player.getCards().add(deck.dealCard());
        player.getCards().add(deck.dealCard());

        PlayerPanel playerPanel1 = new PlayerPanel(this, player, 1, true, true);
        this.add(playerPanel1);

        PlayerPanel playerPanel2 = new PlayerPanel(this, player, 2, false, false);
        this.add(playerPanel2);

        PlayerPanel playerPanel3 = new PlayerPanel(this, player, 3, true, false);
        this.add(playerPanel3);

        PlayerPanel playerPanel4 = new PlayerPanel(this, player, 4, true, false);
        this.add(playerPanel4);

        PlayerPanel playerPanel5 = new PlayerPanel(this, player, 5, true, false);
        this.add(playerPanel5);

        PlayerPanel playerPanel6 = new PlayerPanel(this, player, 6, true, false);
        this.add(playerPanel6);

        PlayerPanel playerPanel7 = new PlayerPanel(this, player, 7, true, false);
        this.add(playerPanel7);

        PlayerPanel playerPanel8 = new PlayerPanel(this, player, 8, true, false);
        this.add(playerPanel8);*/

        // Community cards
        this.initCommunityCards();

        // Action buttons
        this.initAvailableActionsButtons();

        // Background image
        JLabel background = new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/table_1.png"), this.getWidth(), this.getHeight()));
        background.setBounds(this.getInsets().left, this.getInsets().top, this.getWidth(), this.getHeight());
        this.add(background);
    }

    private void initAvailableActionsButtons() {
        List<String> buttonActions = new ArrayList<>();
        buttonActions.add("check");
        buttonActions.add("fold");
        buttonActions.add("call");
        buttonActions.add("bet");
        buttonActions.add("all-in");

        int actionsPosX = 120;
        int actionsPosY = this.getHeight() - 100;
        int buttonWidth = 100;
        int buttonHeight = 30;

        for (int i = 0; i < buttonActions.size(); i++) {
            JButton button = new JButton(buttonActions.get(i));
            button.setActionCommand(button.getText());
            button.setBounds(i * buttonWidth + actionsPosX + (i * 10), actionsPosY, buttonWidth, buttonHeight);
            button.setEnabled(false);
            GamePanel.actionButtons.add(button);
            this.add(button);
            button.addActionListener(new buttonPlayerActionsListener());
        }

        slider = new JSlider(JSlider.HORIZONTAL, 10, 100, 10);
        slider.setBounds(this.getInsets().left, this.getHeight() - 60, this.getWidth(), 60);
        slider.setOpaque(true);
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        this.add(slider);
        slider.addChangeListener(new bettingSliderChangeListener());
    }

    private void initCommunityCards() {
        int communityCardsPosX = 253;
        int communityCardsPosY = 207;

        BufferedImage cards = Utils.loadImage(this, "/poker/client/gui/img/cards.gif");
        BufferedImage cardBackImage = Utils.getSubImage(cards, 0, 724, 125, 181);

        for (int i = 0; i < 5; i++) {
            JLabel label = new JLabel(Utils.getScaledImageAsImageIcon(cardBackImage, cardWidth, cardHeight));
            label.setBounds(i * cardWidth + communityCardsPosX + (i * 10), communityCardsPosY, cardWidth, cardHeight);
            this.add(label);
        }
    }

    public void drawCard(Card card) {
        JLabel label = new JLabel(card.getCardImage());
        label.setBounds(this.getInsets().left, this.getInsets().top, cardWidth, cardHeight);
        label.setVisible(true);
        this.add(label);
    }

    public void drawAllCards(Deck deck) {
        deck = (deck == null) ? new Deck() : deck;
        ImageIcon cardBackImage = null;

        for (int i = 0; i < Card.getSuits().length; i++) {
            for (int j = 0; j < Card.getRanks().length; j++) {
                Card card = deck.dealCard();
                JLabel label = new JLabel(card.getCardImage());
                label.setBounds(j * cardWidth + 75, i * cardHeight + 100, cardWidth, cardHeight);
                this.add(label);

                if (cardBackImage == null) {
                    cardBackImage = card.getCardBackImage();
                }
            }
        }

        JLabel cardBack = new JLabel(cardBackImage);
        cardBack.setBounds(75, 100 + 4 * cardHeight, cardWidth, cardHeight);
        this.add(cardBack);
    }

    static class buttonPlayerActionsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (actionEvent.getActionCommand()) {
                case "check":
                    //System.out.println("[GamePanel] check triggered");
                    getClient().sendData(Utils.getObjectAsBytes("check"));
                    break;
                case "fold":
                    //System.out.println("[GamePanel] fold triggered");
                    getClient().sendData(Utils.getObjectAsBytes("fold"));
                    break;
                case "call":
                    //System.out.println("[GamePanel] call triggered");
                    getClient().sendData(Utils.getObjectAsBytes("call"));
                    break;
                case "bet":
                    //System.out.println("[GamePanel] bet triggered with value of " + slider.getValue());
                    getClient().sendData(Utils.getObjectAsBytes("bet" + String.valueOf(slider.getValue())));
                    break;
                case "all-in":
                    //System.out.println("[GamePanel] all-in triggered");
                    getClient().sendData(Utils.getObjectAsBytes("all-in"));
                    break;
                default:
                    break;
            }
            GamePanel.disableActions();
        }

    }

    static class bettingSliderChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            int val = slider.getValue();

            if (val % 10 < 5) {
                slider.setValue((val / 10) * 10);
            } else {
                slider.setValue((val / 10) * 10 + 10);
            }
        }

    }

}

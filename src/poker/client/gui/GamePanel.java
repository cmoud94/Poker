/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client.gui;

import poker.client.Client;
import poker.game.Card;
import poker.game.Player;
import poker.game.Table;
import poker.utils.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private static List<PlayerPanel> playerPanels;

    private static List<JLabel> communityCards;

    private static JLabel pot;

    public GamePanel(ClientWindow parent, int x, int y, int width, int height, Client client) {
        GamePanel.parent = parent;
        GamePanel.client = client;
        GamePanel.actionButtons = new ArrayList<>();
        GamePanel.playerPanels = new ArrayList<>();
        GamePanel.communityCards = new ArrayList<>();

        this.setBounds(x, y, width, height);
        this.setLayout(null);
        //this.setBorder(new LineBorder(Color.RED));

        this.initComponents();
    }

    public static Client getClient() {
        return client;
    }

    public void newRound() {
        for (JLabel label : GamePanel.communityCards) {
            label.setIcon(null);
        }
        disableActions();
    }

    public void showAvailableActions(List availableActions, int money) {
        slider.setMaximum(money - 10);
        for (JButton button : GamePanel.actionButtons) {
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

    public void drawPlayers(List players, boolean drawAllCards) {
        for (int i = 0; i < players.size(); i++) {
            PlayerPanel playerPanel = playerPanels.get(i);
            Player player = (Player) players.get(i);
            boolean isClient = player.getName().equals(getClient().getName());
            playerPanel.setPlayer(player);
            playerPanel.update(isClient, drawAllCards);
            playerPanel.setVisible(true);
        }
    }

    public void drawCommunityCards(Table table) {
        for (int i = 0; i < table.getCommunityCards().size(); i++) {
            JLabel label = GamePanel.communityCards.get(i);
            Card card = table.getCommunityCards().get(i);
            label.setIcon(card.getCardImage());
        }
    }

    public void updatePot(int money) {
        pot.setText("Pot: " + money);
        pot.setSize(pot.getPreferredSize());
        pot.setVisible(true);
    }

    private void initComponents() {
        // Players
        for (int i = 0; i < 8; i++) {
            PlayerPanel playerPanel = new PlayerPanel(this, (i + 1));
            playerPanels.add(playerPanel);
            this.add(playerPanel);
        }

        // Pot
        pot = new JLabel("Pot: 0");
        pot.setFont(new Font("Sans", Font.BOLD, 12));
        pot.setOpaque(true);
        pot.setBackground(Color.LIGHT_GRAY);
        pot.setSize(pot.getPreferredSize());
        pot.setLocation(375, 290);
        pot.setVisible(false);
        this.add(pot);

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
            actionButtons.add(button);
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

        /*BufferedImage cards = Utils.loadImage(this, "/poker/client/gui/img/cards.gif");
        BufferedImage cardBackImage = Utils.getSubImage(cards, 0, 724, 125, 181);*/

        for (int i = 0; i < 5; i++) {
            JLabel label = new JLabel(); //Utils.getScaledImageAsImageIcon(cardBackImage, cardWidth, cardHeight)
            label.setBounds(i * cardWidth + communityCardsPosX + (i * 10), communityCardsPosY, cardWidth, cardHeight);
            communityCards.add(label);
            this.add(label);
        }
    }

    /*public void drawAllCards(Deck deck) {
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
    }*/

    static class buttonPlayerActionsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (actionEvent.getActionCommand()) {
                case "check":
                    //System.out.println("[GamePanel] check triggered");
                    getClient().sendData(Utils.serialize("check"));
                    break;
                case "fold":
                    //System.out.println("[GamePanel] fold triggered");
                    getClient().sendData(Utils.serialize("fold"));
                    break;
                case "call":
                    //System.out.println("[GamePanel] call triggered");
                    getClient().sendData(Utils.serialize("call"));
                    break;
                case "bet":
                    //System.out.println("[GamePanel] bet triggered with value of " + slider.getValue());
                    getClient().sendData(Utils.serialize("bet " + String.valueOf(slider.getValue())));
                    break;
                case "all-in":
                    //System.out.println("[GamePanel] all-in triggered");
                    getClient().sendData(Utils.serialize("all-in"));
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

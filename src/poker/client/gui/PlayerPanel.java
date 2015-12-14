/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.client.gui;

import poker.game.Player;
import poker.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerPanel extends JPanel {

    private final int type;

    private final GamePanel parent;

    private Player player;

    private final List<JLabel> items;

    // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
    private final int cardWidth = 50;

    private final int cardHeight = 72;

    private List<JLabel> chips;

    private final int chipSize = 40;

    public PlayerPanel(GamePanel parent, int type) {
        this.type = type;
        this.parent = parent;
        this.player = null;
        this.items = new ArrayList<>();
        this.chips = new ArrayList<>();

        chips.add(new JLabel(""));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/dealer_button.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/small_blind.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/big_blind.png"), chipSize, chipSize)));
        chips.get(0).setName("NO_BLIND");
        chips.get(1).setName("DEALER");
        chips.get(2).setName("SMALL_BLIND");
        chips.get(3).setName("BIG_BLIND");

        this.setSize(200, 150);
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(new Color(0, 0, 0, 0));
        this.initComponents(false, false);
        this.setVisible(false);
    }

    /*public PlayerPanel(GamePanel parent, Player player, int type, boolean showCards, boolean client) {
        this.type = type;
        this.parent = parent;
        this.player = player;
        this.items = new ArrayList<>();
        this.chips = new ArrayList<>();

        chips.add(new JLabel(""));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/dealer_button.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/small_blind.png"), chipSize, chipSize)));
        chips.add(new JLabel(Utils.getScaledImageAsImageIcon(Utils.loadImage(this, "/poker/client/gui/img/big_blind.png"), chipSize, chipSize)));
        chips.get(0).setName("NO_BLIND");
        chips.get(1).setName("DEALER");
        chips.get(2).setName("SMALL_BLIND");
        chips.get(3).setName("BIG_BLIND");

        this.setSize(200, 150);
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(new Color(0, 0, 0, 0));
        this.initComponents(showCards, client);
    }*/

    public int getType() {
        return type;
    }

    public GamePanel getParent() {
        return parent;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<JLabel> getItems() {
        return items;
    }

    public void update(boolean isClient, boolean showCards) {
        for (JLabel label : items) {
            switch (label.getName()) {
                case "chip":
                    label = this.getItems().get(this.getItems().indexOf(label));
                    for (JLabel chip : chips) {
                        if (chip.getName().equals(String.valueOf(player.getBlind()))) {
                            label.setIcon(chip.getIcon());
                        }
                    }
                    break;
                case "name":
                    label = this.getItems().get(this.getItems().indexOf(label));
                    label.setText(player.getName() + " (" + player.getMoney() + ")");
                    label.setSize(label.getPreferredSize());
                    if (isClient) {
                        label.setBackground(new Color(0, 255, 255, 200));
                    } else {
                        label.setBackground(Color.LIGHT_GRAY);
                    }
                    break;
                case "card1":
                    label = this.getItems().get(this.getItems().indexOf(label));
                    if (showCards || isClient) {
                        label.setIcon(player.getCards().get(0).getCardImage());
                    } else {
                        label.setIcon(player.getCards().get(0).getCardBackImage());
                    }
                    break;
                case "card2":
                    label = this.getItems().get(this.getItems().indexOf(label));
                    if (showCards || isClient) {
                        label.setIcon(player.getCards().get(1).getCardImage());
                    } else {
                        label.setIcon(player.getCards().get(1).getCardBackImage());
                    }
                    break;
            }
        }
    }

    private void setChipPosition(String name, int posX, int posY) {
        for (JLabel button : chips) {
            if (button.getName().equals(name)) {
                button.setSize(chipSize, chipSize);
                button.setLocation(this.getInsets().left + posX - (button.getWidth() / 2), this.getInsets().top + posY - (button.getHeight() / 2));
                button.setVisible(true);
            } else {
                button.setVisible(false);
            }
        }
    }

    private void initComponents(boolean showCards, boolean isClient) {
        JLabel card1 = new JLabel("");
        JLabel card2 = new JLabel("");
        JLabel name = new JLabel("");
        JLabel chip = new JLabel("");

        card1.setName("card1");
        card2.setName("card2");
        name.setName("name");
        chip.setName("chip");

        String blind = "";

        name.setText("Player name here");
        name.setFont(new Font("Sans", Font.BOLD, 12));
        name.setOpaque(true);
        name.setBackground(Color.LIGHT_GRAY);

        switch (this.getType()) {
            case 1:
                this.setLocation(parent.getInsets().left + 72, parent.getInsets().top + 340);
                card1.setBounds(0, 0, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 5, 0, cardWidth, cardHeight);
                name.setBounds(0, cardHeight + 5, name.getPreferredSize().width, name.getPreferredSize().height);
                this.setChipPosition(blind, 2 * cardWidth + 30, 20);
                break;
            case 2:
                this.setLocation(parent.getInsets().left + 5, parent.getInsets().top + 200);
                card1.setBounds(0, 0, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 5, 0, cardWidth, cardHeight);
                name.setBounds(0, cardHeight + 5, name.getPreferredSize().width, name.getPreferredSize().height);
                this.setChipPosition(blind, 2 * cardWidth + 30, 40);
                break;
            case 3:
                this.setLocation(parent.getInsets().left + 72, parent.getInsets().top + 50);
                name.setBounds(0, 0, name.getPreferredSize().width, name.getPreferredSize().height);
                card1.setBounds(0, name.getHeight() + 5, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 5, name.getHeight() + 5, cardWidth, cardHeight);
                this.setChipPosition(blind, 2 * cardWidth + 30, 100);
                break;
            case 4:
                this.setLocation(parent.getInsets().left + 250, parent.getInsets().top + 20);
                name.setBounds(0, 0, name.getPreferredSize().width, name.getPreferredSize().height);
                card1.setBounds(0, name.getHeight() + 5, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 5, name.getHeight() + 5, cardWidth, cardHeight);
                this.setChipPosition(blind, cardWidth + (chipSize / 2) - 5, name.getHeight() + cardHeight + (chipSize / 2) + 5);
                break;
            case 5:
                this.setLocation(parent.getInsets().left + 430, parent.getInsets().top + 20);
                name.setBounds(0, 0, name.getPreferredSize().width, name.getPreferredSize().height);
                card1.setBounds(0, name.getHeight() + 5, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 5, name.getHeight() + 5, cardWidth, cardHeight);
                this.setChipPosition(blind, cardWidth + (chipSize / 2) - 10, name.getHeight() + cardHeight + (chipSize / 2) + 5);
                break;
            case 6:
                this.setLocation(parent.getInsets().left + 580, parent.getInsets().top + 40);
                name.setBounds(chipSize + 5, 0, name.getPreferredSize().width, name.getPreferredSize().height);
                card1.setBounds(chipSize + 5, name.getHeight() + 5, cardWidth, cardHeight);
                card2.setBounds(chipSize + cardWidth + 10, name.getHeight() + 5, cardWidth, cardHeight);
                this.setChipPosition(blind, 30, name.getHeight() + cardHeight + 20);
                break;
            case 7:
                this.setLocation(parent.getInsets().left + 620, parent.getInsets().top + 200);
                card1.setBounds(chipSize + 5, 0, cardWidth, cardHeight);
                card2.setBounds(chipSize + 10 + cardWidth, 0, cardWidth, cardHeight);
                name.setBounds(chipSize + 5, cardHeight + 5, name.getPreferredSize().width, name.getPreferredSize().height);
                this.setChipPosition(blind, 20, 40);
                break;
            case 8:
                this.setLocation(parent.getInsets().left + 550, parent.getInsets().top + 340);
                card1.setBounds(chipSize + 5, 0, cardWidth, cardHeight);
                card2.setBounds(cardWidth + 10 + chipSize, 0, cardWidth, cardHeight);
                name.setBounds(chipSize + 5, cardHeight + 5, name.getPreferredSize().width, name.getPreferredSize().height);
                this.setChipPosition(blind, 20, 20);
                break;
        }

        card1.setVisible(true);
        card2.setVisible(true);
        name.setVisible(true);
        chip.setVisible(true);

        this.getItems().add(card1);
        this.getItems().add(card2);
        this.getItems().add(name);
        this.getItems().add(chip);

        this.add(card1);
        this.add(card2);
        this.add(name);
        this.add(chip);
    }
}

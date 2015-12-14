/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.game;

import poker.utils.Utils;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Card> deck;

    // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
    private final int cardWidth = 50;

    private final int cardHeight = 72;

    public Deck() {
        // Create the Deck of Cards
        this.deck = new ArrayList<>();

        BufferedImage cards = Utils.loadImage(this, "/poker/client/gui/img/cards.gif");
        ImageIcon cardBackImage = Utils.getScaledImageAsImageIcon(Utils.getSubImage(cards, 0, 724, 125, 181), cardWidth, cardHeight);

        for (int s = 0; s < Card.getSuits().length; s++) {
            for (int r = 0; r < Card.getRanks().length; r++) {
                // Card Dimension WxH: 125x181 ratio: W * 1.448 | H * 0.690607735
                ImageIcon cardImage = Utils.getScaledImageAsImageIcon(Utils.getSubImage(cards, r * 125, s * 181, 125, 181), cardWidth, cardHeight);
                this.getDeck().add(new Card(s, r, cardImage, cardBackImage));
            }
        }

        // Shuffle the deck
        Card tmp;
        for (int i = 0; i < 1000; i++) {
            int index1 = new Random().nextInt(this.getDeck().size());
            int index2 = new Random().nextInt(this.getDeck().size());

            tmp = this.getDeck().get(index1);
            this.getDeck().set(index1, this.getDeck().get(index2));
            this.getDeck().set(index2, tmp);
        }
    }

    public List<Card> getDeck() {
        return deck;
    }

    public Card dealCard() {
        if (this.getDeck().isEmpty()) {
            return null;
        }
        return this.getDeck().remove(0);
    }

}

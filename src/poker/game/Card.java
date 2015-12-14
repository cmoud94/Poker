/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.game;

import javax.swing.*;
import java.io.Serializable;

public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};

    private static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

    private final int suit;

    private final int rank;

    private final ImageIcon cardImage;

    private final ImageIcon cardBackImage;

    public Card(int suit, int rank, ImageIcon cardImage, ImageIcon cardBackImage) {
        this.suit = suit;
        this.rank = rank + 2;
        this.cardImage = cardImage;
        this.cardBackImage = cardBackImage;
    }

    public static String[] getSuits() {
        return suits;
    }

    public static String[] getRanks() {
        return ranks;
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public ImageIcon getCardImage() {
        return cardImage;
    }

    public ImageIcon getCardBackImage() {
        return cardBackImage;
    }

    public String getSuitAsString() {
        return Card.suits[this.getSuit()];
    }

    public String getRankAsString() {
        return (this.getRank() > 10) ? Card.ranks[this.getRank() - 2].substring(0, 1) : Card.ranks[this.getRank() - 2];
    }

    public String toString() {
        return this.getRankAsString() + " | " + this.getSuitAsString();
    }

}

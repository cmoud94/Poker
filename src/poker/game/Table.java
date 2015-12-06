package poker.game;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import java.util.ArrayList;
import java.util.List;

public class Table {

    private final int bigBlind;

    private final List<Card> communityCards;

    private int pot;

    public Table(int bigBlind) {
        this.pot = 0;
        this.bigBlind = bigBlind;
        this.communityCards = new ArrayList<>(5);
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public void printCommunityCards() {
        System.out.println("\033[1mCommunity cards:\033[0m");
        for (Card card : this.getCommunityCards()) {
            System.out.println("\t" + card.toString());
        }
    }
}

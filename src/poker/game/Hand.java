package poker.game;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable {

    private final List<Card> cards;

    private final Player player;

    private final List<Card> hand;

    private String handString;

    private int handStrength;

    private int handCardsValue;

    public Hand(List<Card> communityCards, Player player) {
        this.cards = new ArrayList<>(7);
        this.player = player;
        this.hand = new ArrayList<>(5);
        this.handString = "";
        this.handStrength = 0;
        this.handCardsValue = 0;
        this.cards.addAll(communityCards);
        this.cards.addAll(player.getCards());
    }

    public List<Card> getCards() {
        return cards;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Card> getHand() {
        return hand;
    }

    public String getHandString() {
        return handString;
    }

    private void setHandString(String handString) {
        this.handString = handString;
    }

    public int getHandStrength() {
        return handStrength;
    }

    private void setHandStrength(int handStrength) {
        this.handStrength = handStrength;
    }

    public int getHandCardsValue() {
        return handCardsValue;
    }

    private void setHandCardsValue(int handCardsValue) {
        this.handCardsValue = handCardsValue;
    }

    private void sortCardsByRank() {
        for (int i = 0; i < this.getCards().size(); i++) {
            for (int j = 0; j < this.getCards().size() - 1; j++) {
                int cmp = compareCardRank(this.getCards().get(j), this.getCards().get(j + 1));
                if (cmp == 1) {
                    Card tmp = this.getCards().get(j);
                    this.getCards().set(j, this.getCards().get(j + 1));
                    this.getCards().set(j + 1, tmp);
                }
            }
        }
    }

    private int compareCardRank(Card c1, Card c2) {
        if (c1.getRank() < c2.getRank()) {
            return 1;
        } else if (c1.getRank() > c2.getRank()) {
            return -1;
        }
        return 0;
    }

    private void sortCardsBySuit() {
        for (int i = 0; i < this.getCards().size(); i++) {
            for (int j = 0; j < this.getCards().size() - 1; j++) {
                int cmp = compareCardSuit(this.getCards().get(j), this.getCards().get(j + 1));
                if (cmp == 1) {
                    Card tmp = this.getCards().get(j);
                    this.getCards().set(j, this.getCards().get(j + 1));
                    this.getCards().set(j + 1, tmp);
                }
            }
        }
    }

    private int compareCardSuit(Card c1, Card c2) {
        if (c1.getSuit() < c2.getSuit()) {
            return -1;
        } else if (c1.getSuit() > c2.getSuit()) {
            return 1;
        }
        return 0;
    }

    public boolean check() {
        if (this.checkRoyalFlush()) {
            return true;
        } else if (this.checkStraightFlush()) {
            return true;
        } else if (this.checkFourOfKind()) {
            return true;
        } else if (this.checkFullHouse()) {
            return true;
        } else if (this.checkFlush()) {
            return true;
        } else if (this.checkStraight()) {
            return true;
        } else if (this.checkThreeOfAKind(true)) {
            return true;
        } else if (this.checkTwoPair()) {
            return true;
        } else if (this.checkPair(true, 1, false)) {
            return true;
        } else {
            this.checkHighCard();
        }

        return true;
    }

    private boolean checkRoyalFlush() {
        this.sortCardsByRank();
        this.sortCardsBySuit();

        for (int i = 0; i < 3; i++) {
            if (this.getCards().get(i).getRank() != 14) {
                continue;
            }

            this.getHand().clear();
            int suit = this.getCards().get(i).getSuit();

            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (this.compareCardSuit(c1, c2) != 0 || suit != c1.getSuit() || c1.getRank() != (c2.getRank() + 1)) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Royal Flush");
                    this.setHandStrength(10);
                    this.setHandCardsValue(0);
                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkStraightFlush() {
        this.sortCardsByRank();
        this.sortCardsBySuit();

        for (int i = 0; i < 3; i++) {
            this.getHand().clear();
            int suit = this.getCards().get(i).getSuit();

            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (this.compareCardSuit(c1, c2) != 0 || suit != c1.getSuit() || c1.getRank() != (c2.getRank() + 1)) {
                        if (!(c1.getRank() == 14 && c2.getRank() == 5)) {
                            break;
                        }
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Straight Flush");
                    this.setHandStrength(9);

                    if (this.getHand().get(0).getRank() == 14 && this.getHand().get(1).getRank() == 5) {
                        this.setHandCardsValue(this.getHand().get(1).getRank());
                    } else {
                        this.setHandCardsValue(this.getHand().get(0).getRank());
                    }

                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkFourOfKind() {
        this.sortCardsByRank();

        for (int i = 0; i < 4; i++) {
            this.getHand().clear();
            int rank = this.getCards().get(i).getRank();

            for (int j = 0; j < 4; j++) {
                if (j < 3) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (this.compareCardRank(c1, c2) != 0 || rank != c1.getRank()) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 4) {
                    this.checkHighCard();
                    this.setHandString("Four of a Kind");
                    this.setHandStrength(8);
                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkFullHouse() {
        if (this.checkThreeOfAKind(false)) {
            if (this.checkPair(false, 1, true)) {
                this.setHandString("Full House");
                this.setHandStrength(7);
                this.setHandCardsValue(this.getHand().get(0).getRank());
                return true;
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkFlush() {
        this.sortCardsByRank();
        this.sortCardsBySuit();

        for (int i = 0; i < 3; i++) {
            this.getHand().clear();
            int suit = this.getCards().get(i).getSuit();

            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (this.compareCardSuit(c1, c2) != 0 || suit != c1.getSuit()) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Flush");
                    this.setHandStrength(6);
                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkStraight() {
        this.sortCardsByRank();

        for (int i = 0; i < 3; i++) {
            this.getHand().clear();

            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (c1.getRank() != (c2.getRank() + 1)) {
                        if (!(c1.getRank() == 14 && c2.getRank() == 5)) {
                            break;
                        }
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Straight");
                    this.setHandStrength(5);

                    if (this.getHand().get(0).getRank() == 14 && this.getHand().get(1).getRank() == 5) {
                        this.setHandCardsValue(this.getHand().get(1).getRank());
                    } else {
                        this.setHandCardsValue(this.getHand().get(0).getRank());
                    }

                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkThreeOfAKind(boolean checkHighCard) {
        this.sortCardsByRank();

        for (int i = 0; i < 5; i++) {
            this.getHand().clear();

            for (int j = 0; j < 3; j++) {
                if (j == 0) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);
                    Card c3 = this.getCards().get(i + j + 2);
                    int rankCmp12 = this.compareCardRank(c1, c2);
                    int rankCmp13 = this.compareCardRank(c1, c3);

                    if (rankCmp12 != 0 || rankCmp13 != 0) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 3) {
                    if (checkHighCard) {
                        this.checkHighCard();
                    }

                    this.setHandString("Three of a Kind");
                    this.setHandStrength(4);
                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkTwoPair() {
        if (this.checkPair(false, 1, false)) {
            if (this.checkPair(true, 2, false)) {
                this.setHandString("Two Pair");
                this.setHandStrength(3);
                return true;
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private boolean checkPair(boolean checkHighCard, int numOfPairs, boolean fullHouseCheck) {
        this.sortCardsByRank();
        numOfPairs *= 2;

        if (fullHouseCheck) {
            numOfPairs += 3;
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 2; j++) {
                if (j == 0) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (this.compareCardRank(c1, c2) != 0) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.getHand().add(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == numOfPairs) {
                    if (checkHighCard) {
                        this.checkHighCard();
                    }

                    this.setHandString("Pair");
                    this.setHandStrength(2);
                    return true;
                }
            }
        }

        this.getHand().clear();
        this.setHandStrength(0);
        this.setHandCardsValue(0);
        return false;
    }

    private void checkHighCard() {
        this.sortCardsByRank();

        for (int i = 0; i < this.getCards().size(); i++) {
            if (this.getHand().size() == 5) {
                break;
            }

            if (!this.getHand().contains(this.getCards().get(i))) {
                this.getHand().add(this.getCards().get(i));
                this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i).getRank());
            }
        }

        this.setHandString("High card");
        this.setHandStrength(1);
    }

    public void test() {
        System.out.println(this.getPlayer().getName() + " " + this.getPlayer().getCards());

        System.out.println("Found: " + this.getHandString());
        for (Card card : this.getHand()) {
            System.out.println("\t" + card.toString());
        }

        System.out.println("\nHand Strength: " + this.getHandStrength());
        System.out.println("Hand cards value: " + this.getHandCardsValue());
    }

}

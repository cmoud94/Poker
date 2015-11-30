package poker.game;

import java.util.ArrayList;
import java.util.List;

public class Hand {

    private final List<Card> cards;

    private final Player player;

    private List<Card> hand;

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
        return this.cards;
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<Card> getHand() {
        return this.hand;
    }

    private void addToHand(Card card) {
        this.hand.add(card);
    }

    public String getHandString() {
        return this.handString;
    }

    private void setHandString(String handString) {
        this.handString = handString;
    }

    public int getHandStrength() {
        return this.handStrength;
    }

    private void setHandStrength(int strength) {
        this.handStrength = strength;
    }

    public int getHandCardsValue() {
        return this.handCardsValue;
    }

    private void setHandCardsValue(int value) {
        this.handCardsValue = value;
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
        } else if (this.checkHighCard()) {
            return true;
        }
        return false;
    }

    private boolean checkRoyalFlush() {
        this.sortCardsByRank();
        this.sortCardsBySuit();

        for (int i = 0; i < 3; i++) {
            if (!this.getCards().get(i).getRankAsString().equals("Ace")) {
                continue;
            }
            int suit = this.getCards().get(i).getSuit();
            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);
                    int suitCmp = this.compareCardSuit(c1, c2);

                    if (suitCmp != 0 || suit != c1.getSuit() || c1.getRank() != (c2.getRank() + 1)) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Royal Flush");
                    this.setHandStrength(10);
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
            int suit = this.getCards().get(i).getSuit();
            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);
                    int suitCmp = this.compareCardSuit(c1, c2);

                    if (suitCmp != 0 || suit != c1.getSuit() || c1.getRank() != (c2.getRank() + 1)) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Straight Flush");
                    this.setHandStrength(9);
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
            for (int j = 0; j < 4; j++) {
                if (j == 0) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);
                    Card c3 = this.getCards().get(i + j + 2);
                    Card c4 = this.getCards().get(i + j + 3);
                    int rankCmp12 = this.compareCardRank(c1, c2);
                    int rankCmp13 = this.compareCardRank(c1, c3);
                    int rankCmp14 = this.compareCardRank(c1, c4);

                    if (rankCmp12 != 0 || rankCmp13 != 0 || rankCmp14 != 0) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
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
                return true;
            }
        }
        this.getHand().clear();
        return false;
    }

    private boolean checkFlush() {
        this.sortCardsByRank();
        this.sortCardsBySuit();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == 0) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);
                    Card c3 = this.getCards().get(i + j + 2);
                    Card c4 = this.getCards().get(i + j + 3);
                    Card c5 = this.getCards().get(i + j + 4);
                    int suitCmp12 = this.compareCardSuit(c1, c2);
                    int suitCmp13 = this.compareCardSuit(c1, c3);
                    int suitCmp14 = this.compareCardSuit(c1, c4);
                    int suitCmp15 = this.compareCardSuit(c1, c5);

                    if (suitCmp12 != 0 || suitCmp13 != 0 || suitCmp14 != 0 || suitCmp15 != 0) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
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
            for (int j = 0; j < 5; j++) {
                if (j < 4) {
                    Card c1 = this.getCards().get(i + j);
                    Card c2 = this.getCards().get(i + j + 1);

                    if (c1.getRank() != (c2.getRank() + 1)) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
                    this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i + j).getRank());
                }

                if (this.getHand().size() == 5) {
                    this.setHandString("Straight");
                    this.setHandStrength(5);
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
                    this.addToHand(this.getCards().get(i + j));
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
                    int rankCmp = this.compareCardRank(c1, c2);

                    if (rankCmp != 0) {
                        break;
                    }
                }

                if (!this.getHand().contains(this.getCards().get(i + j))) {
                    this.addToHand(this.getCards().get(i + j));
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

    private boolean checkHighCard() {
        this.sortCardsByRank();

        for (int i = 0; i < this.getCards().size(); i++) {
            if (this.getHand().size() == 5) {
                break;
            }
            if (!this.getHand().contains(this.getCards().get(i))) {
                this.addToHand(this.getCards().get(i));
                this.setHandCardsValue(this.getHandCardsValue() + this.getCards().get(i).getRank());
            }
        }
        this.setHandString("High card");
        this.setHandStrength(1);
        return true;
    }

    // TODO: Smazat
    public void test() {
        System.out.println(this.getPlayer().getName() + " " + this.getPlayer().getCards());

        /*System.out.println("Cards: ");
        for (Card card : this.getCards()) {
            System.out.println("\t" + card.toString());
        }*/

        System.out.println("Found: " + this.getHandString());
        for (Card card : this.getHand()) {
            System.out.println("\t" + card.toString());
        }

        System.out.println("Hand Strength: " + this.getHandStrength());
        System.out.println("Hand cards value: " + this.getHandCardsValue());
    }

}

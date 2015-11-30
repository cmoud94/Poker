package poker.game;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private int pot;

    private final int bigBlind;

    private final List<Card> communityCards;

    public Table(int bigBlind) {
        this.pot = 0;
        this.bigBlind = bigBlind;
        this.communityCards = new ArrayList<>(5);
    }

    public int getPot() {
        return this.pot;
    }

    public void addToPot(int money) {
        this.pot += money;
    }

    public int getBigBlind() {
        return this.bigBlind;
    }

    public List<Card> getCommunityCards() {
        return this.communityCards;
    }

    public void addCommunityCard(Card card) {
        this.communityCards.add(card);
    }

}

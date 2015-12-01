package poker.game;

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
}

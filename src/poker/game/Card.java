package poker.game;

public class Card {

    private static final String[] suits = {"Hearts", "Clubs", "Spades", "Diamonds"};

    private static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

    private final int suit;

    private final int rank;

    public Card(int suit, int rank) {
        this.suit = suit;
        this.rank = rank + 2;
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

    public String getSuitAsString() {
        return Card.suits[this.getSuit()];
    }

    public String getRankAsString() {
        return Card.ranks[this.getRank() - 2];
    }

    public String toString() {
        return this.getSuitAsString() + " | " + this.getRankAsString();
    }
}
